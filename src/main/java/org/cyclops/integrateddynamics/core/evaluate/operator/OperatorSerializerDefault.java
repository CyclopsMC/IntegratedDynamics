package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
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
    public String getUniqueName() {
        return null; // Only the default serializer can have name null
    }

    @Override
    public INBT serialize(IOperator operator) {
        return new StringNBT(operator.getUniqueName());
    }

    @Override
    public IOperator deserialize(INBT value) {
        IOperator operator = Operators.REGISTRY.getOperator(value.getString());
        if (operator != null) {
            return operator;
        }
        return null;
    }
}
