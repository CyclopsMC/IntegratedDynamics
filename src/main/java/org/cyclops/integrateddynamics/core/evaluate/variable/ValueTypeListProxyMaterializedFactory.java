package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

/**
 * Factory for {@link ValueTypeListProxyMaterialized}.
 * @author rubensworks
 */
public class ValueTypeListProxyMaterializedFactory implements IValueTypeListProxyFactoryTypeRegistry.IProxyFactory<IValueType<IValue>, IValue, ValueTypeListProxyMaterialized<IValueType<IValue>, IValue>> {

    @Override
    public String getName() {
        return "materialized";
    }

    @Override
    public INBT serialize(ValueTypeListProxyMaterialized<IValueType<IValue>, IValue> values) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        CompoundNBT tag = new CompoundNBT();
        ListNBT list = new ListNBT();

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
        tag.putString("valueType", valueType.getTranslationKey());
        tag.put("values", list);

        // Store values
        for (IValue value : values) {
            INBT valueSerialized = ValueHelpers.serializeRaw(value);
            if(heterogeneous) {
                CompoundNBT valueTag = new CompoundNBT();
                valueTag.putString("valueType", value.getType().getTranslationKey());
                valueTag.put("value", valueSerialized);
                list.add(valueTag);
            } else {
                list.add(valueSerialized);
            }
        }

        return tag;
    }

    @Override
    public ValueTypeListProxyMaterialized<IValueType<IValue>, IValue> deserialize(INBT value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        if (!(value instanceof CompoundNBT)) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the materialized list value '%s' as it is not a CompoundNBT.", value));
        }
        CompoundNBT tag = (CompoundNBT) value;
        if (!tag.contains("valueType", Constants.NBT.TAG_STRING)) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the materialized list value '%s' as it is missing a valueType.", value));
        }
        if (!tag.contains("values", Constants.NBT.TAG_LIST)) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the materialized list value '%s' as it is missing values.", value));
        }

        String valueTypeName = tag.getString("valueType");
        IValueType<IValue> valueType = ValueTypes.REGISTRY.getValueType(valueTypeName);
        if (valueType == null) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the serialized materialized list proxy value because the value type by name '%s' was not found.", valueTypeName));
        }

        boolean heterogeneous = valueType.isCategory();
        IValueType<IValue> elementValueType = valueType;

        ImmutableList.Builder<IValue> builder = ImmutableList.builder();
        ListNBT list = (ListNBT) tag.get("values");
        for (INBT valueTag : list) {
            INBT valueSerialized;
            if (heterogeneous) {
                String subValueTypeName = ((CompoundNBT) valueTag).getString("valueType");
                elementValueType = ValueTypes.REGISTRY.getValueType(subValueTypeName);
                if (elementValueType == null) {
                    throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the serialized materialized list proxy value because the value type by name '%s' was not found.", subValueTypeName));
                }
                valueSerialized = ((CompoundNBT) valueTag).get("value");
            } else {
                valueSerialized = valueTag;
            }
            IValue deserializedValue = ValueHelpers.deserializeRaw(elementValueType, valueSerialized);
            builder.add(deserializedValue);
        }

        return new ValueTypeListProxyMaterialized<>(valueType, builder.build());
    }
}
