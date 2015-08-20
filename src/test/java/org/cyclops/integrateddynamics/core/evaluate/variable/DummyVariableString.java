package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Dummy string variable.
 * @author rubensworks
 */
public class DummyVariableString extends DummyVariable<ValueTypeString.ValueString> {

    public DummyVariableString(ValueTypeString.ValueString value) {
        super(ValueTypes.STRING, value);
    }

    public DummyVariableString() {
        super(ValueTypes.STRING);
    }

}
