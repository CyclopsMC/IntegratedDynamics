package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.resources.ResourceLocation;
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

    protected void registerModelResourceLocation() {
        ValueTypes.REGISTRY.getClient().registerValueTypeModel(getValueType(),
                ResourceLocation.parse(getValueType().getModId() + ":valuetype" + getValueType().getTypeNamespace().replace('.', '/') + getValueType().getTypeName().replace('.', '/')));
    }

}
