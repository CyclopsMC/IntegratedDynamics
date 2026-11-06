package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * Infers types in an expression tree and collects structured type errors.
 * @author rubensworks
 */
public final class ExpressionTypeChecker {

    private ExpressionTypeChecker() {
    }

    /**
     * Type-check the given expression.
     * @param expression The expression.
     * @param refTypes Resolves the output type of a referenced variable id, may return null when unknown.
     * @return The result.
     */
    public static Result check(Expression expression, IntFunction<IValueType<?>> refTypes) {
        Checker checker = new Checker(expression, refTypes);
        IVariable<?> root = checker.visit(expression.root(), new int[0]);
        return new Result(root.getType(), Collections.unmodifiableList(checker.errors), checker.emptySlots,
                Collections.unmodifiableMap(checker.types));
    }

    /**
     * @param expression An expression with parameters.
     * @return The operator signature of the expression.
     */
    public static IValueType<?>[] getInputTypes(Expression expression) {
        IValueType<?>[] types = new IValueType[expression.parameters().size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = expression.parameters().get(i).type();
        }
        return types;
    }

    /**
     * The result of type checking.
     * @param outputType The inferred output type of the root node.
     * @param errors All type errors.
     * @param emptySlots The number of empty slots.
     * @param nodeTypes The inferred output type of each node, by path.
     */
    public static record Result(IValueType<?> outputType, List<Error> errors, int emptySlots,
                                Map<IntList, IValueType<?>> nodeTypes) {
        public boolean isValid() {
            return errors.isEmpty() && emptySlots == 0;
        }

        /**
         * @param path A node path.
         * @return The inferred output type of the node at this path, or null if there is no such node.
         */
        @Nullable
        public IValueType<?> getType(int[] path) {
            return nodeTypes.get(IntArrayList.wrap(path));
        }

        /**
         * @param path A node path.
         * @param slot A slot index of that node.
         * @return The first error for the given slot, or null.
         */
        @Nullable
        public Error getError(int[] path, int slot) {
            for (Error error : errors) {
                if (error.slot() == slot && Arrays.equals(error.path(), path)) {
                    return error;
                }
            }
            return null;
        }

        /**
         * @param root The root node of the expression.
         * @param path The path of a node.
         * @return The messages of the errors that are shown on that node in the text, or null if it has none:
         *         the error of its slot for a value, or all errors of an operator, one per line, for the parts of the operator itself.
         */
        @Nullable
        public Component getErrorMessage(ExpressionNode root, int[] path) {
            if (!ExpressionTrees.isOperatorPath(root, path)) {
                Error error = path.length == 0 ? null : getError(ExpressionTrees.parent(path), path[path.length - 1]);
                return error == null ? null : error.message();
            }
            MutableComponent message = null;
            for (Error error : errors) {
                if (Arrays.equals(error.path(), path)) {
                    message = message == null ? error.message().copy() : message.append("\n").append(error.message());
                }
            }
            return message;
        }

        /**
         * @param path A node path.
         * @return If the node at this path or any node below it has an error.
         */
        public boolean hasErrorWithin(int[] path) {
            for (Error error : errors) {
                if (error.path().length >= path.length && Arrays.equals(Arrays.copyOf(error.path(), path.length), path)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @param path A node path.
         * @return If the operator node at this path or any of its slots has an error.
         */
        public boolean hasErrorAt(int[] path) {
            for (Error error : errors) {
                if (Arrays.equals(error.path(), path)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A type error.
     * @param path The path to the operator node with the error.
     * @param slot The slot index of the operator node, or -1 if the error applies to the node as a whole.
     * @param expected The expected type, or null if not applicable.
     * @param actual The actual type, or null if not applicable.
     * @param message A human-readable message.
     */
    public static record Error(int[] path, int slot, @Nullable IValueType<?> expected, @Nullable IValueType<?> actual,
                               MutableComponent message) {}

    /**
     * A variable that only knows its type, used during type checking.
     * @param <V> The value type.
     */
    public static class TypedVariable<V extends IValue> extends VariableAdapter<V> {
        private final IValueType<V> type;

        public TypedVariable(IValueType<V> type) {
            this.type = type;
        }

        @Override
        public IValueType<V> getType() {
            return type;
        }

        @Override
        public V getValue() throws EvaluationException {
            throw new EvaluationException(Component.translatable(L10NValues.EXPRESSION_ERROR_NOVALUE));
        }
    }

    private static class Checker {
        private final Expression expression;
        private final IntFunction<IValueType<?>> refTypes;
        private final List<Error> errors = new ArrayList<>();
        private final Map<IntList, IValueType<?>> types = new HashMap<>();
        private int emptySlots = 0;

        private Checker(Expression expression, IntFunction<IValueType<?>> refTypes) {
            this.expression = expression;
            this.refTypes = refTypes;
        }

        private IVariable<?> visit(ExpressionNode node, int[] path) {
            IVariable<?> variable = visitNode(node, path);
            types.put(IntArrayList.wrap(path), variable.getType());
            return variable;
        }

        private IVariable<?> visitNode(ExpressionNode node, int[] path) {
            if (node instanceof ExpressionNode.Operator op) {
                return visitOperator(op, path);
            }
            if (node instanceof ExpressionNode.Empty) {
                emptySlots++;
            }
            return getLeafVariable(node, expression.parameters(), refTypes);
        }

        private IVariable<?> visitOperator(ExpressionNode.Operator node, int[] path) {
            IOperator operator = node.operator();
            IValueType[] expectedTypes = operator.getInputTypes();
            IVariable[] inputs = new IVariable[node.children().size()];
            IValueType[] inputTypes = new IValueType[inputs.length];
            boolean complete = true;
            boolean slotErrors = false;
            for (int i = 0; i < inputs.length; i++) {
                ExpressionNode child = node.children().get(i);
                inputs[i] = visit(child, ExpressionTrees.append(path, i));
                inputTypes[i] = inputs[i].getType();
                if (child instanceof ExpressionNode.Empty) {
                    complete = false;
                } else if (i < expectedTypes.length && !ValueHelpers.correspondsTo(expectedTypes[i], inputTypes[i])) {
                    slotErrors = true;
                    errors.add(new Error(path, i, expectedTypes[i], inputTypes[i],
                            Component.translatable(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                                    operator.getLocalizedNameFull(),
                                    Component.translatable(inputTypes[i].getTranslationKey()),
                                    Integer.toString(i + 1),
                                    Component.translatable(expectedTypes[i].getTranslationKey()))));
                }
            }
            if (complete && !slotErrors) {
                MutableComponent error = operator.validateTypes(inputTypes);
                if (error != null) {
                    errors.add(new Error(path, -1, null, null, error));
                }
            }
            return getOutputVariable(operator, inputs);
        }
    }

    private static IVariable<?> getLeafVariable(ExpressionNode node, List<ExpressionParameter> parameters,
                                                IntFunction<IValueType<?>> refTypes) {
        return switch (node) {
            case ExpressionNode.Const constant -> new Variable<>(constant.value());
            case ExpressionNode.Ref ref -> {
                IValueType<?> type = refTypes.apply(ref.variableId());
                yield new TypedVariable(type != null ? type : ValueTypes.CATEGORY_ANY);
            }
            case ExpressionNode.Param param -> new TypedVariable((IValueType) (param.index() < parameters.size()
                    ? parameters.get(param.index()).type() : ValueTypes.CATEGORY_ANY));
            default -> new TypedVariable<>(ValueTypes.CATEGORY_ANY);
        };
    }

    private static IVariable<?> getOutputVariable(IOperator operator, IVariable[] inputs) {
        IValueType<?> outputType;
        try {
            outputType = operator.getConditionalOutputType(inputs);
        } catch (RuntimeException e) {
            outputType = null;
        }
        if (outputType == null) {
            outputType = operator.getOutputType();
        }
        return new TypedVariable(outputType);
    }

    /**
     * Infers the output types of nodes that are built bottom-up, such as while parsing.
     * Types are remembered per node, so long chains such as a + b + c are not checked again for each operator.
     */
    public static class Inference {
        private final List<ExpressionParameter> parameters;
        private final IntFunction<IValueType<?>> refTypes;
        private final Map<ExpressionNode, IVariable<?>> variables = new IdentityHashMap<>();

        /**
         * @param parameters The parameters of the expression the nodes belong to.
         * @param refTypes Resolves the output type of a referenced variable id, may return null when unknown.
         */
        public Inference(List<ExpressionParameter> parameters, IntFunction<IValueType<?>> refTypes) {
            this.parameters = parameters;
            this.refTypes = refTypes;
        }

        /**
         * @param node A node.
         * @return Its inferred output type, as {@link #check} gives it.
         */
        public IValueType<?> getType(ExpressionNode node) {
            return getVariable(node).getType();
        }

        private IVariable<?> getVariable(ExpressionNode node) {
            IVariable<?> variable = variables.get(node);
            if (variable == null) {
                if (node instanceof ExpressionNode.Operator op) {
                    IVariable[] inputs = new IVariable[op.children().size()];
                    for (int i = 0; i < inputs.length; i++) {
                        inputs[i] = getVariable(op.children().get(i));
                    }
                    variable = getOutputVariable(op.operator(), inputs);
                } else {
                    variable = getLeafVariable(node, parameters, refTypes);
                }
                variables.put(node, variable);
            }
            return variable;
        }
    }

}
