package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.ImmutableList;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

/**
 * Factory for {@link ValueTypeListProxyMaterialized}.
 * @author rubensworks
 */
public class ValueTypeListProxyMaterializedFactory implements IValueTypeListProxyFactoryTypeRegistry.IProxyFactory<IValueType<IValue>, IValue, ValueTypeListProxyMaterialized<IValueType<IValue>, IValue>> {

    private static final String ELEMENT_DELIMITER = ";";
    private static final String ELEMENT_DELIMITER_SPLITREGEX = "(?<!\\\\);";
    private static final String ELEMENT_DELIMITER_ESCAPED = "\\\\;";

    @Override
    public String getName() {
        return "materialized";
    }

    @Override
    public String serialize(ValueTypeListProxyMaterialized<IValueType<IValue>, IValue> values) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        StringBuilder sb = new StringBuilder();
        IValueType<IValue> valueType = values.getValueType();
        boolean heterogeneous = false;
        try {
            // Hack to avoid issue where categories are sometimes used to serialize/deserialize,
            // which is not allowed (and will crash during deserialization #570).
            if (valueType.isCategory() && values.getLength() > 0) {
                heterogeneous = true;
            }
        } catch (EvaluationException e) {}
        sb.append(valueType.getTranslationKey());
        for (IValue value : values) {
            if(heterogeneous) {
                sb.append(ELEMENT_DELIMITER);
                sb.append(value.getType().getTranslationKey());
            }
            sb.append(ELEMENT_DELIMITER);
            sb.append(ValueHelpers.serializeRaw(value).replaceAll(ELEMENT_DELIMITER, ELEMENT_DELIMITER_ESCAPED));
        }
        return sb.toString();
    }

    @Override
    public ValueTypeListProxyMaterialized<IValueType<IValue>, IValue> deserialize(String value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        String[] split = value.split(ELEMENT_DELIMITER_SPLITREGEX);
        if (split.length < 1) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the serialized materialized list proxy value '%s'.", value));
        }

        String valueTypeName = split[0];
        IValueType<IValue> valueType = ValueTypes.REGISTRY.getValueType(valueTypeName);
        if (valueType == null) {
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the serialized materialized list proxy value because the value type by name '%s' was not found.", valueTypeName));
        }
        boolean heterogeneous = valueType.isCategory();
        IValueType<IValue> elementValueType = valueType;

        ImmutableList.Builder<IValue> builder = ImmutableList.builder();
        for (int i = 1; i < split.length; ++i) {
            if (heterogeneous) {
                elementValueType = ValueTypes.REGISTRY.getValueType(split[i]);
                if (elementValueType == null) {
                    throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Could not deserialize the serialized materialized list proxy value because the value type by name '%s' was not found.", split[i]));
                }
                ++i;
                if (i >= split.length) {
                    throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(String.format("Detected invalid heterogeneous serialized materialized list proxy value for the value '%s'.", value));
                }
            }
            String serializedValue = split[i];
            IValue deserializedValue = ValueHelpers.deserializeRaw(elementValueType, serializedValue.replaceAll(ELEMENT_DELIMITER_ESCAPED, ELEMENT_DELIMITER));
            builder.add(deserializedValue);
        }

        return new ValueTypeListProxyMaterialized<>(valueType, builder.build());
    }
}
