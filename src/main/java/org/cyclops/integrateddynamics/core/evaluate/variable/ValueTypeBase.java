package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
    private final TextFormatting colorFormat;
    @Nullable // TODO: remove Nullable option in 1.15
    private final Class<V> valueClass;

    private String translationKey = null;

    @Deprecated // TODO: remove, and also remove Nullable option in 1.15
    public ValueTypeBase(String typeName, int color, TextFormatting colorFormat) {
        this(typeName, color, colorFormat, null);
    }

    public ValueTypeBase(String typeName, int color, TextFormatting colorFormat, @Nullable Class<V> valueClass) {
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
    public TextFormatting getDisplayColorFormat() {
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
    public void loadTooltip(List<ITextComponent> lines, boolean appendOptionalInfo, @Nullable V value) {
        String typeName = L10NHelpers.localize(getTranslationKey());
        lines.add(new TranslationTextComponent(L10NValues.VALUETYPE_TOOLTIP_TYPENAME, getDisplayColorFormat() + typeName));
        if(appendOptionalInfo) {
            L10NHelpers.addOptionalInfo(lines, getUnlocalizedPrefix());
        }
    }

    @Override
    public ITextComponent canDeserialize(INBT value) {
        try {
            deserialize(value);
            return null;
        } catch (IllegalArgumentException e) {
            return new TranslationTextComponent(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, value);
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
        // TODO remove null check in 1.15
        if (this.valueClass == null) {
            return (V) value;
        }
        try {
            return this.valueClass.cast(value);
        } catch (ClassCastException e) {
            throw new EvaluationException(String.format("Attempted to cast %s to %s, for value \"%s\"",
                    L10NHelpers.localize(value.getType().getTranslationKey()),
                    L10NHelpers.localize(this.getTranslationKey()),
                    value.getType().toCompactString(value)
            ));
        }
    }
}
