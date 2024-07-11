package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeCategory;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for {@link IValueType}.
 * @author rubensworks
 */
public final class ValueTypeRegistry implements IValueTypeRegistry {

    private static ValueTypeRegistry INSTANCE = new ValueTypeRegistry();
    private static final IValueTypeVariableFacade INVALID_FACADE = new ValueTypeVariableFacade(false, null, (IValue) null);

    private final Map<String, IValueType> valueTypes = Maps.newHashMap();
    @OnlyIn(Dist.CLIENT)
    private Map<IValueType, ResourceLocation> valueTypeModels;

    private ValueTypeRegistry() {
        if(MinecraftHelpers.isModdedEnvironment()) {
            if(MinecraftHelpers.isClientSide()) {
                valueTypeModels = new IdentityHashMap<>();
            }
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
        valueTypes.put(valueType.getUniqueName().toString(), valueType);
        return valueType;
    }

    @Override
    public <V extends IValue, T extends IValueTypeCategory<V>> T registerCategory(T category) {
        return register(category);
    }

    @Override
    public IValueType getValueType(ResourceLocation name) {
        return valueTypes.get(name.toString());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <V extends IValue, T extends IValueType<V>> void registerValueTypeModel(T valueType, ResourceLocation modelLocation) {
        valueTypeModels.put(valueType, modelLocation);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <V extends IValue, T extends IValueType<V>> ResourceLocation getValueTypeModel(T valueType) {
        return valueTypeModels.get(valueType);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Collection<ResourceLocation> getValueTypeModels() {
        return Collections.unmodifiableCollection(valueTypeModels.values());
    }

    @Override
    public Collection<IValueType> getValueTypes() {
        return Collections.unmodifiableCollection(valueTypes.values());
    }

    @Override
    public ResourceLocation getUniqueName() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "valuetype");
    }

    @Override
    public IValueTypeVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        if(!tag.contains("typeName", Tag.TAG_STRING)
                || !tag.contains("value")) {
            return INVALID_FACADE;
        }
        IValueType type = getValueType(ResourceLocation.parse(tag.getString("typeName")));
        if(type == null) {
            return INVALID_FACADE;
        }
        IValue value;
        try {
            value = ValueHelpers.deserializeRaw(valueDeseralizationContext, type, tag.get("value"));
        } catch (IllegalArgumentException e) {
            return INVALID_FACADE;
        }
        return new ValueTypeVariableFacade(id, type, value);
    }

    @Override
    public void setVariableFacade(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, IValueTypeVariableFacade variableFacade) {
        tag.putString("typeName", variableFacade.getValueType().getUniqueName().toString());
        tag.put("value", ValueHelpers.serializeRaw(valueDeseralizationContext, variableFacade.getValue()));
    }

    @Override
    public boolean isInstance(IVariableFacade variableFacade) {
        return variableFacade instanceof IValueTypeVariableFacade;
    }

    @Override
    public boolean isInstance(IVariable<?> variable) {
        return variable instanceof IVariable;
    }

    public static class ValueTypeVariableFacadePredicate extends VariableFacadePredicate<IValueTypeVariableFacade> {

        private final Optional<IValueType> valueType;
        private final Optional<ValuePredicate> valuePredicate;

        public ValueTypeVariableFacadePredicate(Optional<IValueType> valueType, Optional<ValuePredicate> valuePredicate) {
            super(IValueTypeVariableFacade.class);
            this.valueType = valueType;
            this.valuePredicate = valuePredicate;
        }

        public Optional<IValueType> getValueType() {
            return valueType;
        }

        public Optional<ValuePredicate> getValuePredicate() {
            return valuePredicate;
        }

        @Override
        protected boolean testTyped(IValueTypeVariableFacade variableFacade) {
            return super.testTyped(variableFacade)
                    && (valueType.isEmpty() || ValueHelpers.correspondsTo(variableFacade.getValueType(), valueType.get()))
                    && valuePredicate.orElse(ValuePredicate.ANY).test(variableFacade.getValue());
        }
    }
}
