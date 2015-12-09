package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Dummy string variable.
 * @author rubensworks
 */
public class DummyVariableNamed extends DummyVariable<ValueTypeString.ValueString> {

    public DummyVariableNamed(ValueTypeString.ValueString value) {
        super(ValueTypes.STRING, value);
    }

    public DummyVariableNamed() {
        super(ValueTypes.STRING);
    }

}
