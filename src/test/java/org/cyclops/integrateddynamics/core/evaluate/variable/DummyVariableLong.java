package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Dummy long variable (based on {@link DummyVariableInteger}).
 * @author met4000
 */
public class DummyVariableLong extends DummyVariable<ValueTypeLong.ValueLong> {

    public DummyVariableLong(ValueTypeLong.ValueLong value) {
        super(ValueTypes.LONG, value);
    }

    public DummyVariableLong() {
        super(ValueTypes.LONG);
    }

}
