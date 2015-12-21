package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Dummy string variable.
 * @author rubensworks
 */
public class DummyVariableList extends DummyVariable<ValueTypeList.ValueList> {

    public DummyVariableList(ValueTypeList.ValueList value) {
        super(ValueTypes.LIST, value);
    }

    public DummyVariableList() {
        super(ValueTypes.LIST);
    }

}
