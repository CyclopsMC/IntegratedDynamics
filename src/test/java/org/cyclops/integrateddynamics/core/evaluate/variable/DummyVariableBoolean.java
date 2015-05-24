package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Dummy boolean variable.
 * @author rubensworks
 */
public class DummyVariableBoolean extends DummyVariable<ValueTypeBoolean.ValueBoolean> {

    public DummyVariableBoolean(ValueTypeBoolean.ValueBoolean value) {
        super(ValueTypes.BOOLEAN, value);
    }

    public DummyVariableBoolean() {
        super(ValueTypes.BOOLEAN);
    }

}
