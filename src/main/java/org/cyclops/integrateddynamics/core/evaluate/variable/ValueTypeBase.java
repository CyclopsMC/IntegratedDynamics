package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeStringLPElement;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base implementation of a value type.
 * @author rubensworks
 */
public abstract class ValueTypeBase<V extends IValue> implements IValueType<V> {

    private final String typeName;
    private final int color;
    private final ChatFormatting colorFormat;
    private final Class<V> valueClass;

    private String translationKey = null;

    public ValueTypeBase(String typeName, int color, ChatFormatting colorFormat, Class<V> valueClass) {
        this.typeName = typeName;
        this.color = color;
        this.colorFormat = colorFormat;
        this.valueClass = valueClass;
        if(MinecraftHelpers.isModdedEnvironment() && MinecraftHelpers.isClientSide()) {
            registerModelResourceLocation();
        }
    }

    @Override
    public boolean isCategory() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return new ResourceLocation(getModId(), getTypeName());
    }

    protected String getUnlocalizedPrefix() {
        return "valuetype." + getModId() + getTypeNamespace() + getTypeName();
    }

    protected String getTypeNamespace() {
        return ".";
    }

    @Override
    public String getTranslationKey() {
        return translationKey != null ? translationKey : (translationKey = getUnlocalizedPrefix());
    }

    @Override
    public String getTypeName() {
        return this.typeName;
    }

    @Override
    public int getDisplayColor() {
        return this.color;
    }

    @Override
    public ChatFormatting getDisplayColorFormat() {
        return this.colorFormat;
    }

    @Override
    public boolean correspondsTo(IValueType<?> valueType) {
        return this == valueType;
    }

    @OnlyIn(Dist.CLIENT)
    protected void registerModelResourceLocation() {
        ValueTypes.REGISTRY.registerValueTypeModel(this,
                new ResourceLocation(getModId() + ":valuetype" + getTypeNamespace().replace('.', '/') + getTypeName().replace('.', '/')));
    }

    @Override
    public void loadTooltip(List<Component> lines, boolean appendOptionalInfo, @Nullable V value) {
        String typeName = L10NHelpers.localize(getTranslationKey());
        lines.add(new TranslatableComponent(L10NValues.VALUETYPE_TOOLTIP_TYPENAME, getDisplayColorFormat() + typeName));
        if(appendOptionalInfo) {
            L10NHelpers.addOptionalInfo(lines, getUnlocalizedPrefix());
        }
    }

    @Override
    public Component canDeserialize(Tag value) {
        try {
            deserialize(value);
            return null;
        } catch (IllegalArgumentException e) {
            return new TranslatableComponent(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, value);
        }
    }

    @Override
    public V materialize(V value) throws EvaluationException {
        return value;
    }

    @Override
    public V parseString(String value) throws EvaluationException {
        throw new UnsupportedOperationException("parseString is not supported on value type " + this);
    }

    @Override
    public String toString(V value) {
        throw new UnsupportedOperationException("toString is not supported on value type " + this);
    }

    @Override
    public String toString() {
        return L10NHelpers.localize(getTranslationKey());
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeStringLPElement(this);
    }

    protected String getModId() {
        return Reference.MOD_ID;
    }

    @Override
    public V cast(IValue value) throws EvaluationException {
        try {
            return this.valueClass.cast(value);
        } catch (ClassCastException e) {
            throw new EvaluationException(new TranslatableComponent(L10NValues.OPERATOR_ERROR_CAST_ILLEGAL,
                    new TranslatableComponent(value.getType().getTranslationKey()),
                    new TranslatableComponent(this.getTranslationKey()),
                    value.getType().toCompactString(value)
            ));
        }
    }
}
