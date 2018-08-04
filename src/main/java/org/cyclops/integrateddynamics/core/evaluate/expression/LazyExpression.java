package org.cyclops.integrateddynamics.core.evaluate.expression;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.IExpression;
import org.cyclops.integrateddynamics.api.evaluate.expression.ILazyExpressionValueCache;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;

/**
 * A generic expression with arbitrarily nested binary operations.
 * This is evaluated in a lazy manner.
 * @author rubensworks
 */
public class LazyExpression<V extends IValue> extends VariableAdapter<V> implements IExpression<V> {

    private final int id;
    private final IOperator op;
    private final IVariable[] input;
    private final ILazyExpressionValueCache valueCache;
    private boolean errored = false;

    public LazyExpression(int id, IOperator op, IVariable[] input, ILazyExpressionValueCache valueCache) {
        this.id = id;
        this.op = op;
        this.input = input;
        this.valueCache = valueCache;

        // Make sure that any previous values become un-cached,
        // so that the first evaluation of this expression is guaranteed to happen.
        valueCache.removeValue(id);
    }

    @Override
    public IValue evaluate() throws EvaluationException {
        if(valueCache.hasValue(id)) {
            return valueCache.getValue(id);
        }
        IValue value = op.evaluate(input);
        for (IVariable inputVariable : input) {
            inputVariable.addInvalidationListener(this);
        }
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
    public V getValue() throws EvaluationException {
        IValue value;
        try {
            value = evaluate();
        } catch (EvaluationException e) {
            errored = true;
            throw new EvaluationException(e.getMessage());
        }
        try {
            return (V) value;
        } catch (ClassCastException e) {
            errored = true;
            throw new EvaluationException(String.format("The evaluation for operator %s returned %s instead of " +
                    "the expected %s.", op, value.getType(), op.getOutputType()));
        }
    }

    @Override
    public void invalidate() {
        valueCache.removeValue(id);
        super.invalidate();
    }

    public IOperator getOperator() {
        return op;
    }

    public IVariable[] getInput() {
        return input;
    }
}
