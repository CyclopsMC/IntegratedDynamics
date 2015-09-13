package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Dummy double variable.
 * @author rubensworks
 */
public class DummyVariableDouble extends DummyVariable<ValueTypeDouble.ValueDouble> {

    public DummyVariableDouble(ValueTypeDouble.ValueDouble value) {
        super(ValueTypes.DOUBLE, value);
    }

    public DummyVariableDouble() {
        super(ValueTypes.DOUBLE);
    }

}
