package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.ILazyExpressionValueCache;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.function.IntFunction;

/**
 * Turns expression trees into variables.
 * @author rubensworks
 */
public final class ExpressionEvaluator {

    private ExpressionEvaluator() {
    }

    /**
     * Build a variable tree of cached lazy expressions, for a parameterless expression in a network.
     * The root operator node is cached in the given value cache under the given id,
     * inner operator nodes are cached in a cache that is local to this tree.
     * @param root The root node.
     * @param rootId The id under which the root is cached.
     * @param rootCache The value cache of the root, usually the network.
     * @param refs Resolves referenced variables, may return null if not available.
     * @return The root variable, or null if a reference could not be resolved.
     */
    @Nullable
    public static IVariable<?> buildLazy(ExpressionNode root, int rootId, ILazyExpressionValueCache rootCache,
                                         IntFunction<IVariable<?>> refs) {
        return new LazyBuilder(rootId, rootCache, refs).build(root, true);
    }

    /**
     * Build a variable for a single evaluation, where parameters are bound to the given variables.
     * Operator nodes are evaluated lazily and at most once.
     * @param node The node.
     * @param params The parameter values.
     * @param refs Bound referenced variables.
     * @return The variable.
     * @throws EvaluationException If a node can not be bound.
     */
    public static IVariable<?> buildBound(ExpressionNode node, IVariable[] params, Int2ObjectMap<IVariable<?>> refs) throws EvaluationException {
        return switch (node) {
            case ExpressionNode.Const constant -> new Variable<>(constant.value());
            case ExpressionNode.Param param -> {
                if (param.index() < 0 || param.index() >= params.length) {
                    throw new EvaluationException(Component.translatable(L10NValues.EXPRESSION_ERROR_INCOMPLETE));
                }
                yield params[param.index()];
            }
            case ExpressionNode.Ref ref -> {
                IVariable<?> variable = refs.get(ref.variableId());
                if (variable == null) {
                    EvaluationException exception = new EvaluationException(Component.translatable(
                            L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK, Integer.toString(ref.variableId())));
                    exception.setRetryEvaluation(true);
                    throw exception;
                }
                yield variable;
            }
            case ExpressionNode.Empty empty -> throw new EvaluationException(Component.translatable(L10NValues.EXPRESSION_ERROR_INCOMPLETE));
            case ExpressionNode.Operator op -> {
                IVariable[] inputs = new IVariable[op.children().size()];
                for (int i = 0; i < inputs.length; i++) {
                    inputs[i] = buildBound(op.children().get(i), params, refs);
                }
                yield new OnceVariable<>(op.operator(), inputs);
            }
        };
    }

    private static class LazyBuilder {
        private final int rootId;
        private final ILazyExpressionValueCache rootCache;
        private final ILazyExpressionValueCache localCache = new LocalValueCache();
        private final IntFunction<IVariable<?>> refs;
        private int nextLocalId = 0;

        private LazyBuilder(int rootId, ILazyExpressionValueCache rootCache, IntFunction<IVariable<?>> refs) {
            this.rootId = rootId;
            this.rootCache = rootCache;
            this.refs = refs;
        }

        @Nullable
        private IVariable<?> build(ExpressionNode node, boolean isRoot) {
            return switch (node) {
                case ExpressionNode.Const constant -> new Variable<>(constant.value());
                case ExpressionNode.Ref ref -> refs.apply(ref.variableId());
                case ExpressionNode.Param param -> null;
                case ExpressionNode.Empty empty -> null;
                case ExpressionNode.Operator op -> {
                    IVariable[] inputs = new IVariable[op.children().size()];
                    for (int i = 0; i < inputs.length; i++) {
                        inputs[i] = build(op.children().get(i), false);
                        if (inputs[i] == null) {
                            yield null;
                        }
                    }
                    yield isRoot
                            ? new LazyExpression<>(rootId, op.operator(), inputs, rootCache)
                            : new LazyExpression<>(nextLocalId++, op.operator(), inputs, localCache);
                }
            };
        }
    }

    /**
     * A value cache that is local to a single expression tree.
     */
    public static class LocalValueCache implements ILazyExpressionValueCache {
        private final Int2ObjectMap<IValue> values = new Int2ObjectOpenHashMap<>();

        @Override
        public void setValue(int id, IValue value) {
            values.put(id, value);
        }

        @Override
        public boolean hasValue(int id) {
            return values.containsKey(id);
        }

        @Override
        public IValue getValue(int id) {
            return values.get(id);
        }

        @Override
        public void removeValue(int id) {
            values.remove(id);
        }
    }

    /**
     * A variable that evaluates an operator at most once, without registering invalidation listeners.
     * @param <V> The value type.
     */
    public static class OnceVariable<V extends IValue> extends VariableAdapter<V> {
        private final IOperator operator;
        private final IVariable[] inputs;
        private V value;

        public OnceVariable(IOperator operator, IVariable[] inputs) {
            this.operator = operator;
            this.inputs = inputs;
        }

        @Override
        public IValueType<V> getType() {
            return operator.getConditionalOutputType(inputs);
        }

        @Override
        public V getValue() throws EvaluationException {
            if (value == null) {
                value = (V) operator.evaluate(inputs);
            }
            return value;
        }
    }

}
