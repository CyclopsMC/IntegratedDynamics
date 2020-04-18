package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;

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
    public INBT serialize(IOperator operator) {
        return StringNBT.valueOf(operator.getUniqueName().toString());
    }

    @Override
    public IOperator deserialize(INBT value) {
        return Operators.REGISTRY.getOperator(new ResourceLocation(value.getString()));
    }
}
