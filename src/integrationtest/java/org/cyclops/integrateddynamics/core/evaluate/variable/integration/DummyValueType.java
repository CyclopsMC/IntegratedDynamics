package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import javax.annotation.Nullable;
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
    public ResourceLocation getUniqueName() {
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "boolean";
    }

    @Override
    public void loadTooltip(List<Component> lines, boolean appendOptionalInfo, @Nullable DummyValue value) {

    }

    @Override
    public MutableComponent toCompactString(DummyValue value) {
        return Component.literal("dummy");
    }

    @Override
    public int getDisplayColor() {
        return 0;
    }

    @Override
    public ChatFormatting getDisplayColorFormat() {
        return ChatFormatting.WHITE;
    }

    @Override
    public boolean correspondsTo(IValueType valueType) {
        return false;
    }

    @Override
    public Tag serialize(DummyValue value) {
        return EndTag.INSTANCE;
    }

    @Override
    public Component canDeserialize(Tag value) {
        return null;
    }

    @Override
    public DummyValue deserialize(Tag value) {
        return DummyValue.of();
    }

    @Override
    public DummyValue materialize(DummyValue value) {
        return value;
    }

    @Override
    public String toString(DummyValue value) {
        return "DUMMY STRING";
    }

    @Override
    public DummyValue parseString(String value) throws EvaluationException {
        return DummyValue.of();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return null;
    }

    @Override
    public DummyValue cast(IValue value) throws EvaluationException {
        return (DummyValue) value;
    }

    public static class DummyValue extends ValueBase {

        private DummyValue() {
            super(TYPE);
        }

        public static DummyValue of() {
            return new DummyValue();
        }

        @Override
        public String toString() {
            return "DummyValue";
        }
    }

}
