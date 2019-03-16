package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import lombok.ToString;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

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
    public String getTypeName() {
        return "boolean";
    }

    @Override
    public String getTranslationKey() {
        return "boolean";
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo, DummyValue value) {

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

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return null;
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
