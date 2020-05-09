package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.advancement.criterion.JsonDeserializers;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeCategory;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistry;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Registry for {@link IValueType}.
 * @author rubensworks
 */
public final class ValueTypeRegistry implements IValueTypeRegistry {

    private static ValueTypeRegistry INSTANCE = new ValueTypeRegistry();
    private static final IValueTypeVariableFacade INVALID_FACADE = new ValueTypeVariableFacade(false, null, (IValue) null);

    private final Map<String, IValueType> valueTypes = Maps.newHashMap();
    @SideOnly(Side.CLIENT)
    private Map<IValueType, ResourceLocation> valueTypeModels;

    private ValueTypeRegistry() {
        if(MinecraftHelpers.isClientSide()) {
            valueTypeModels = new IdentityHashMap<>();
        }
        if(MinecraftHelpers.isModdedEnvironment()) {
            IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).registerHandler(this);
        }
    }

    /**
     * @return The unique instance.
     */
    public static ValueTypeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <V extends IValue, T extends IValueType<V>> T register(T valueType) {
        valueTypes.put(valueType.getTranslationKey(), valueType);
        return valueType;
    }

    @Override
    public <V extends IValue, T extends IValueTypeCategory<V>> T registerCategory(T category) {
        return register(category);
    }

    @Override
    public IValueType getValueType(String name) {
        return valueTypes.get(name);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public <V extends IValue, T extends IValueType<V>> void registerValueTypeModel(T valueType, ResourceLocation modelLocation) {
        valueTypeModels.put(valueType, modelLocation);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public <V extends IValue, T extends IValueType<V>> ResourceLocation getValueTypeModel(T valueType) {
        return valueTypeModels.get(valueType);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Collection<ResourceLocation> getValueTypeModels() {
        return Collections.unmodifiableCollection(valueTypeModels.values());
    }

    @Override
    public Collection<IValueType> getValueTypes() {
        return Collections.unmodifiableCollection(valueTypes.values());
    }

    @Override
    public String getTypeId() {
        return "valuetype";
    }

    @Override
    public IValueTypeVariableFacade getVariableFacade(int id, NBTTagCompound tag) {
        if(!tag.hasKey("typeName", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())
                || !tag.hasKey("value", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())) {
            return INVALID_FACADE;
        }
        IValueType type = getValueType(tag.getString("typeName"));
        if(type == null) {
            return INVALID_FACADE;
        }
        IValue value = ValueHelpers.deserializeRaw(type, tag.getString("value"));
        return new ValueTypeVariableFacade(id, type, value);
    }

    @Override
    public void setVariableFacade(NBTTagCompound tag, IValueTypeVariableFacade variableFacade) {
        tag.setString("typeName", variableFacade.getValueType().getTranslationKey());
        tag.setString("value", ValueHelpers.serializeRaw(variableFacade.getValue()));
    }

    @Override
    public VariableFacadePredicate deserializeVariableFacadePredicate(JsonObject element) {
        IValueType valueType = JsonDeserializers.deserializeValueType(element);
        return new AspectVariableFacadePredicate(valueType, JsonDeserializers.deserializeValue(element, valueType));
    }

    public static class AspectVariableFacadePredicate extends VariableFacadePredicate<IValueTypeVariableFacade> {

        private final IValueType valueType;
        private final ValuePredicate valuePredicate;

        public AspectVariableFacadePredicate(@Nullable IValueType valueType, ValuePredicate valuePredicate) {
            super(IValueTypeVariableFacade.class);
            this.valueType = valueType;
            this.valuePredicate = valuePredicate;
        }

        @Override
        protected boolean testTyped(IValueTypeVariableFacade variableFacade) {
            return super.testTyped(variableFacade)
                    && (valueType == null || ValueHelpers.correspondsTo(variableFacade.getValueType(), valueType))
                    && valuePredicate.test(variableFacade.getValue());
        }
    }
}
