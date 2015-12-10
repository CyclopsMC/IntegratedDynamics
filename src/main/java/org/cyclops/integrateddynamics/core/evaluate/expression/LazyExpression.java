package org.cyclops.integrateddynamics.core.evaluate.expression;

import org.apache.logging.log4j.Level;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.IExpression;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;

/**
 * A generic expression with arbitrarily nested binary operations.
 * This is evaluated in a lazy manner.
 * @author rubensworks
 */
public class LazyExpression<V extends IValue> implements IExpression<V> {

    private final int id;
    private final IOperator op;
    private final IVariable[] input;
    private final IValueCache valueCache;
    private boolean errored = false;

    public LazyExpression(int id, IOperator op, IVariable[] input, IValueCache valueCache) {
        this.id = id;
        this.op = op;
        this.input = input;
        this.valueCache = valueCache;
    }

    @Override
    public IValue evaluate() throws EvaluationException {
        if(valueCache.hasValue(id)) {
            return valueCache.getValue(id);
        }
        IValue value = op.evaluate(input);
        valueCache.setValue(id, value);
        return value;
    }

    @Override
    public boolean hasErrored() {
        return errored;
    }

    @Override
    public IValueType<V> getType() {
        return op.getConditionalOutputType(input);
    }

    @Override
    public V getValue() {
        IValue value = null;
        try {
            value = evaluate();
        } catch (EvaluationException e) {
            errored = true;
            e.printStackTrace(); // TODO: delegate to some error-log
            return getType().getDefault();
        }
        try {
            return (V) value;
        } catch (ClassCastException e) {
            IntegratedDynamics.clog(Level.ERROR, String.format("The evaluation for operator %s returned %s instead of " +
                    "the expected %s.", op, value.getType(), op.getOutputType()));
            return null;
        }
    }

    /**
     * Cache object that is responsible for storing values of this expression.
     * This cache object is responsible for determining when the values need to be ejected from the cache.
     */
    public static interface IValueCache {

        public void setValue(int id, IValue value);
        public boolean hasValue(int id);
        public IValue getValue(int id);

    }

}
