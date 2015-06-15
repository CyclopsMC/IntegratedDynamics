package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Dummy integer variable.
 * @author rubensworks
 */
public class DummyVariableInteger extends DummyVariable<ValueTypeInteger.ValueInteger> {

    public DummyVariableInteger(ValueTypeInteger.ValueInteger value) {
        super(ValueTypes.INTEGER, value);
    }

    public DummyVariableInteger() {
        super(ValueTypes.INTEGER);
    }

}
