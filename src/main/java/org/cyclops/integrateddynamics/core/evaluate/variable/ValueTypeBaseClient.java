package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.resources.Identifier;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeClient;

/**
 * @author rubensworks
 */
public class ValueTypeBaseClient<V extends IValue> implements IValueTypeClient<V> {

    private final ValueTypeBase<V> valueType;

    public ValueTypeBaseClient(ValueTypeBase<V> valueType) {
        this.valueType = valueType;
    }

    public ValueTypeBase<V> getValueType() {
        return valueType;
    }

    protected void registerModelIdentifier() {
        ValueTypes.REGISTRY.getClient().registerValueTypeModel(getValueType(),
                Identifier.parse(getValueType().getModId() + ":valuetype" + getValueType().getTypeNamespace().replace('.', '/') + getValueType().getTypeName().replace('.', '/')));
    }

}
