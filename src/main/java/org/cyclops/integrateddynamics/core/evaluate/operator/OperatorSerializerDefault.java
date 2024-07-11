package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

/**
 * The default serializer for operators.
 * @author rubensworks
 */
public class OperatorSerializerDefault implements IOperatorSerializer<IOperator> {

    @Override
    public boolean canHandle(IOperator operator) {
        return true;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return null; // Only the default serializer can have name null
    }

    @Override
    public Tag serialize(ValueDeseralizationContext valueDeseralizationContext, IOperator operator) {
        return StringTag.valueOf(operator.getUniqueName().toString());
    }

    @Override
    public IOperator deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) {
        return Operators.REGISTRY.getOperator(ResourceLocation.parse(value.getAsString()));
    }
}
