package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeStringLPElement;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Base implementation of a value type.
 * @author rubensworks
 */
public abstract class ValueTypeBase<V extends IValue> implements IValueType<V> {

    private final String typeName;
    private final int color;
    private final ChatFormatting colorFormat;
    private final Class<V> valueClass;
    private ValueTypeBaseClient<V> client;

    private String translationKey = null;

    public ValueTypeBase(String typeName, int color, ChatFormatting colorFormat, Class<V> valueClass) {
        this.typeName = typeName;
        this.color = color;
        this.colorFormat = colorFormat;
        this.valueClass = valueClass;
        if(IModHelpers.get().getMinecraftHelpers().isModdedEnvironment() && IModHelpers.get().getMinecraftHelpers().isClientSide()) {
            this.client = constructClient();
            this.client.registerModelIdentifier();
        }
    }

    protected ValueTypeBaseClient<V> constructClient() {
        return new ValueTypeBaseClient<>(this);
    }

    @Override
    public ValueTypeBaseClient<V> getClient() {
        return client;
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
    public Identifier getUniqueName() {
        return Identifier.fromNamespaceAndPath(getModId(), getTypeName());
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

    @Override
    public void loadTooltip(Consumer<Component> tooltipAdder, boolean appendOptionalInfo, @Nullable V value) {
        String typeName = IModHelpers.get().getL10NHelpers().localize(getTranslationKey());
        tooltipAdder.accept(Component.translatable(L10NValues.VALUETYPE_TOOLTIP_TYPENAME, getDisplayColorFormat() + typeName));
        if(appendOptionalInfo) {
            IModHelpers.get().getL10NHelpers().addOptionalInfo(tooltipAdder, getUnlocalizedPrefix(), net.minecraft.world.item.TooltipFlag.NORMAL);
        }
    }

    @Override
    public Component canDeserialize(ValueInput valueInput) {
        try {
            deserialize(valueInput);
            return null;
        } catch (IllegalArgumentException e) {
            return Component.translatable(L10NValues.VALUETYPE_ERROR_INVALIDINPUT, valueInput);
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
        return IModHelpers.get().getL10NHelpers().localize(getTranslationKey());
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
            throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_CAST_ILLEGAL,
                    Component.translatable(value.getType().getTranslationKey()),
                    Component.translatable(this.getTranslationKey()),
                    value.getType().toCompactString(value)
            ));
        }
    }
}
