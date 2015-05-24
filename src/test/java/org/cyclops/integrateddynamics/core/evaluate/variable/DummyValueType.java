package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;

/**
 * Dummy value type
 * @author rubensworks
 */
public class DummyValueType implements IValueType<DummyValueType.DummyValue> {

    public static final DummyValueType TYPE = new DummyValueType();

    @Override
    public DummyValue getDefault() {
        return null;
    }

    @Override
    public String getTypeName() {
        return "boolean";
    }

    @ToString
    public static class DummyValue extends BaseValue {

        private DummyValue() {
            super(TYPE);
        }

        public static DummyValue of() {
            return new DummyValue();
        }

    }

}
