package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.*;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.item.TagPathElement;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.slf4j.Logger;

import java.util.*;

/**
 * Registry for {@link IValueType}.
 * @author rubensworks
 */
public final class ValueTypeRegistry implements IValueTypeRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static ValueTypeRegistry INSTANCE = new ValueTypeRegistry();
    private static final IValueTypeVariableFacade INVALID_FACADE = new ValueTypeVariableFacade(false, null, (IValue) null);

    private final Map<String, IValueType> valueTypes = Maps.newHashMap();
    private ValueTypeRegistryClient client;

    private ValueTypeRegistry() {
        if(IModHelpers.get().getMinecraftHelpers().isModdedEnvironment()) {
            if(IModHelpers.get().getMinecraftHelpers().isClientSide()) {
                client = new ValueTypeRegistryClient();
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
    public IValueTypeRegistryClient getClient() {
        return this.client;
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
    public IValueType getValueType(Identifier name) {
        return valueTypes.get(name.toString());
    }

    @Override
    public Collection<IValueType> getValueTypes() {
        return Collections.unmodifiableCollection(valueTypes.values());
    }

    @Override
    public Identifier getUniqueName() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "valuetype");
    }

    @Override
    public IValueTypeVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        if(!tag.contains("typeName") || !tag.contains("value")) {
            return INVALID_FACADE;
        }
        IValueType type = getValueType(Identifier.parse(tag.getString("typeName").orElseThrow()));
        if(type == null) {
            return INVALID_FACADE;
        }
        IValue value;
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(tag), LOGGER)) {
            ValueInput input = TagValueInput.create(
                    scopedCollector,
                    valueDeseralizationContext.holderLookupProvider(),
                    tag.getCompound("value").orElseThrow()
            );
            value = ValueHelpers.deserializeRaw(input, type);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return INVALID_FACADE;
        }
        return new ValueTypeVariableFacade(id, type, value);
    }

    @Override
    public void setVariableFacade(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, IValueTypeVariableFacade variableFacade) {
        tag.putString("typeName", variableFacade.getValueType().getUniqueName().toString());
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(tag), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, valueDeseralizationContext.holderLookupProvider());
            ValueHelpers.serializeRaw(valueOutput, variableFacade.getValue());
            tag.put("value", valueOutput.buildResult());
        }
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
