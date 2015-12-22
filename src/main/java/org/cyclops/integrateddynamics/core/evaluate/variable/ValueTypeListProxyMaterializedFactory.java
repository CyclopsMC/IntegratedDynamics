package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.util.List;

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
        sb.append(valueType.getUnlocalizedName());
        for (IValue value : values) {
            sb.append(ELEMENT_DELIMITER);
            sb.append(valueType.serialize(value).replaceAll(ELEMENT_DELIMITER, ELEMENT_DELIMITER_ESCAPED));
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
        String[] values = new String[split.length - 1];
        System.arraycopy(split, 1, values, 0, split.length - 1);

        List<IValue> list = Lists.newArrayList();
        for (String serializedValue : values) {
            IValue deserializedValue = valueType.deserialize(serializedValue.replaceAll(ELEMENT_DELIMITER_ESCAPED, ELEMENT_DELIMITER));
            list.add(deserializedValue);
        }

        return new ValueTypeListProxyMaterialized<>(valueType, list);
    }
}
