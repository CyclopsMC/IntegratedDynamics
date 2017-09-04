package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Dummy NBT variable.
 * @author rubensworks
 */
public class DummyVariableNbt extends DummyVariable<ValueTypeNbt.ValueNbt> {

    public DummyVariableNbt(ValueTypeNbt.ValueNbt value) {
        super(ValueTypes.NBT, value);
    }

    public DummyVariableNbt() {
        super(ValueTypes.NBT);
    }

}
