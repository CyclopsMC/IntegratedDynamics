package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
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
import java.util.Collections;
import java.util.List;
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
        return new Result(root.getType(), Collections.unmodifiableList(checker.errors), checker.emptySlots);
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
     */
    public static record Result(IValueType<?> outputType, List<Error> errors, int emptySlots) {
        public boolean isValid() {
            return errors.isEmpty() && emptySlots == 0;
        }

        /**
         * @param path A node path.
         * @param slot A slot index of that node.
         * @return The first error for the given slot, or null.
         */
        @Nullable
        public Error getError(int[] path, int slot) {
            for (Error error : errors) {
                if (error.slot() == slot && java.util.Arrays.equals(error.path(), path)) {
                    return error;
                }
            }
            return null;
        }

        /**
         * @param path A node path.
         * @return If the node at this path or any node below it has an error.
         */
        public boolean hasErrorWithin(int[] path) {
            for (Error error : errors) {
                if (error.path().length >= path.length && java.util.Arrays.equals(java.util.Arrays.copyOf(error.path(), path.length), path)) {
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
                if (java.util.Arrays.equals(error.path(), path)) {
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
        private int emptySlots = 0;

        private Checker(Expression expression, IntFunction<IValueType<?>> refTypes) {
            this.expression = expression;
            this.refTypes = refTypes;
        }

        private IVariable<?> visit(ExpressionNode node, int[] path) {
            return switch (node) {
                case ExpressionNode.Empty empty -> {
                    emptySlots++;
                    yield new TypedVariable<>(ValueTypes.CATEGORY_ANY);
                }
                case ExpressionNode.Const constant -> new Variable<>(constant.value());
                case ExpressionNode.Ref ref -> {
                    IValueType<?> type = refTypes.apply(ref.variableId());
                    yield new TypedVariable(type != null ? type : ValueTypes.CATEGORY_ANY);
                }
                case ExpressionNode.Param param -> new TypedVariable((IValueType) (param.index() < expression.parameters().size()
                        ? expression.parameters().get(param.index()).type() : ValueTypes.CATEGORY_ANY));
                case ExpressionNode.Operator op -> visitOperator(op, path);
            };
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
    }

}
