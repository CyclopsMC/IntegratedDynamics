package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import java.util.List;

/**
 * Dummy value type
 * @author rubensworks
 */
public class DummyValueType implements IValueType<DummyValueType.DummyValue> {

    public static final DummyValueType TYPE = new DummyValueType();

    @Override
    public boolean isCategory() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public DummyValue getDefault() {
        return null;
    }

    @Override
    public String getUnlocalizedName() {
        return "boolean";
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo) {

    }

    @Override
    public String toCompactString(DummyValue value) {
        return "dummy";
    }

    @Override
    public int getDisplayColor() {
        return 0;
    }

    @Override
    public String getDisplayColorFormat() {
        return "";
    }

    @Override
    public boolean correspondsTo(IValueType valueType) {
        return false;
    }

    @Override
    public String serialize(DummyValue value) {
        return null;
    }

    @Override
    public L10NHelpers.UnlocalizedString canDeserialize(String value) {
        return null;
    }

    @Override
    public DummyValue deserialize(String value) {
        return null;
    }

    @Override
    public DummyValue materialize(DummyValue value) {
        return value;
    }

    @ToString
    public static class DummyValue extends ValueBase {

        private DummyValue() {
            super(TYPE);
        }

        public static DummyValue of() {
            return new DummyValue();
        }

    }

}
