package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Dummy boolean variable.
 * @author rubensworks
 */
public class DummyVariableOperator extends DummyVariable<ValueTypeOperator.ValueOperator> {

    public DummyVariableOperator(ValueTypeOperator.ValueOperator value) {
        super(ValueTypes.OPERATOR, value);
    }

    public DummyVariableOperator() {
        super(ValueTypes.OPERATOR);
    }

}
