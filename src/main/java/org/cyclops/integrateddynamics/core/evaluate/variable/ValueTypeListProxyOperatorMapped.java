package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;

/**
 * A list proxy for a list that is mapped to another list by an operator.
 */
public class ValueTypeListProxyOperatorMapped extends ValueTypeListProxyBase<IValueType<IValue>, IValue> {

    private final IOperator operator;
    private final IValueTypeListProxy listProxy;

    public ValueTypeListProxyOperatorMapped(IOperator operator, IValueTypeListProxy listProxy) {
        super(ValueTypeListProxyFactories.MATERIALIZED.getName(), operator.getInputTypes().length == 1 ? operator.getOutputType() : (IValueType) ValueTypes.OPERATOR);
        this.operator = operator;
        this.listProxy = listProxy;
    }

    @Override
    public int getLength() throws EvaluationException {
        return listProxy.getLength();
    }

    @Override
    public IValue get(int index) throws EvaluationException {
        IValue value = listProxy.get(index);
        return ValueHelpers.evaluateOperator(operator, value);
    }
}
