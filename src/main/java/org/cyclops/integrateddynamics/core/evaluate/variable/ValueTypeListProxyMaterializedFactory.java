package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

/**
 * Factory for {@link ValueTypeListProxyMaterialized}.
 * @author rubensworks
 */
public class ValueTypeListProxyMaterializedFactory implements IValueTypeListProxyFactoryTypeRegistry.IProxyFactory<IValueType<IValue>, IValue, ValueTypeListProxyMaterialized<IValueType<IValue>, IValue>> {

    @Override
    public ResourceLocation getName() {
        return new ResourceLocation(Reference.MOD_ID, "materialized");
    }

    @Override
    public Tag serialize(ValueTypeListProxyMaterialized<IValueType<IValue>, IValue> values) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        // Store headers
        IValueType<IValue> valueType = values.getValueType();
        boolean heterogeneous = false;
        try {
            // Hack to avoid issue where categories are sometimes used to serialize/deserialize,
            // which is not allowed (and will crash during deserialization #570).
            if (valueType.isCategory() && values.getLength() > 0) {
                heterogeneous = true;
            }
        } catch (EvaluationException e) {}
        tag.putString("valueType", valueType.getUniqueName().toString());
        tag.put("values", list);

        // Store values
        for (IValue value : values) {
            Tag valueSerialized = ValueHelpers.serializeRaw(value);
            if(heterogeneous) {
                CompoundTag valueTag = new CompoundTag();
                valueTag.putString("valueType", value.getType().getUniqueName().toString());
                valueTag.put("value", valueSerialized);
                list.add(valueTag);
            } else {
                list.add(valueSerialized);
            }
        }

        return tag;
    }

    @Override
    public ValueTypeListProxyMaterialized<IValueType<IValue>, IValue> deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        if (!(value instanceof CompoundTag)) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the materialized list value '%s' as it is not a CompoundTag.", value));
        }
        CompoundTag tag = (CompoundTag) value;
        if (!tag.contains("valueType", Tag.TAG_STRING)) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the materialized list value '%s' as it is missing a valueType.", value));
        }
        if (!tag.contains("values", Tag.TAG_LIST)) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the materialized list value '%s' as it is missing values.", value));
        }

        String valueTypeName = tag.getString("valueType");
        IValueType<IValue> valueType = ValueTypes.REGISTRY.getValueType(new ResourceLocation(valueTypeName));
        if (valueType == null) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the serialized materialized list proxy value because the value type by name '%s' was not found.", valueTypeName));
        }

        boolean heterogeneous = valueType.isCategory();
        IValueType<IValue> elementValueType = valueType;

        ImmutableList.Builder<IValue> builder = ImmutableList.builder();
        ListTag list = (ListTag) tag.get("values");
        for (Tag valueTag : list) {
            Tag valueSerialized;
            if (heterogeneous) {
                String subValueTypeName = ((CompoundTag) valueTag).getString("valueType");
                elementValueType = ValueTypes.REGISTRY.getValueType(new ResourceLocation(subValueTypeName));
                if (elementValueType == null) {
                    throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the serialized materialized list proxy value because the value type by name '%s' was not found.", subValueTypeName));
                }
                valueSerialized = ((CompoundTag) valueTag).get("value");
            } else {
                valueSerialized = valueTag;
            }
            IValue deserializedValue = ValueHelpers.deserializeRaw(valueDeseralizationContext, elementValueType, valueSerialized);
            builder.add(deserializedValue);
        }

        return new ValueTypeListProxyMaterialized<>(valueType, builder.build());
    }
}
