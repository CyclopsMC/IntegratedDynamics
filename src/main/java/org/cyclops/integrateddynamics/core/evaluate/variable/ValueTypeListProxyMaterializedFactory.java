package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.Reference;
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
    public ResourceLocation getName() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "materialized");
    }

    @Override
    public void serialize(ValueOutput valueOutput, ValueTypeListProxyMaterialized<IValueType<IValue>, IValue> values) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        ValueOutput.ValueOutputList list = valueOutput.childrenList("values");

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
        valueOutput.putString("valueType", valueType.getUniqueName().toString());

        // Store values
        for (IValue value : values) {
            ValueOutput valueTag = list.addChild();
            if(heterogeneous) {
                valueTag.putString("valueType", value.getType().getUniqueName().toString());
            }
            ValueHelpers.serializeRaw(valueTag, value);
        }
    }

    @Override
    public ValueTypeListProxyMaterialized<IValueType<IValue>, IValue> deserialize(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        // TODO: is this still needed? If not, remove this.
//        // This tag rewrite needed for loading variables in advancement icons
//        if (tag.contains("values", Tag.TAG_BYTE_ARRAY)) {
//            byte[] byteArray = tag.getByteArray("values");
//            ListTag list = new ListTag();
//            for (byte b : byteArray) {
//                list.add(IntTag.valueOf(b));
//            }
//            tag.put("values", list);
//        }

        String valueTypeName = valueInput.getString("valueType").orElseThrow();
        IValueType<IValue> valueType = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(valueTypeName));
        if (valueType == null) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the serialized materialized list proxy value because the value type by name '%s' was not found.", valueTypeName));
        }

        boolean heterogeneous = valueType.isCategory();
        IValueType<IValue> elementValueType = valueType;

        ImmutableList.Builder<IValue> builder = ImmutableList.builder();
        ValueInput.ValueInputList list = valueInput.childrenList("values").orElseThrow();
        for (ValueInput valueTag : list) {
            if (heterogeneous) {
                String subValueTypeName = valueTag.getString("valueType").orElseThrow();
                elementValueType = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(subValueTypeName));
                if (elementValueType == null) {
                    throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the serialized materialized list proxy value because the value type by name '%s' was not found.", subValueTypeName));
                }
            }
            IValue deserializedValue = ValueHelpers.deserializeRaw(valueTag, elementValueType);
            builder.add(deserializedValue);
        }

        return new ValueTypeListProxyMaterialized<>(valueType, builder.build());
    }
}
