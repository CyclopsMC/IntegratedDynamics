package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void serialize(ValueOutput valueOutput, IOperator operator) {
        valueOutput.putString("v", operator.getUniqueName().toString());
    }

    @Override
    public IOperator deserialize(ValueInput valueInput) {
        return Operators.REGISTRY.getOperator(ResourceLocation.parse(valueInput.getString("v").orElseThrow()));
    }
}
