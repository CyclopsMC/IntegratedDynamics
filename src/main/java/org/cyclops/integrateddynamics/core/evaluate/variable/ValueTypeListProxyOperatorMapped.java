package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

/**
 * A list proxy for a list that is mapped to another list by an operator.
 */
public class ValueTypeListProxyOperatorMapped extends ValueTypeListProxyBase<IValueType<IValue>, IValue> {

    private final IOperator operator;
    private final IValueTypeListProxy listProxy;

    public ValueTypeListProxyOperatorMapped(IOperator operator, IValueTypeListProxy listProxy) {
        super(ValueTypeListProxyFactories.MAPPED.getName(), operator.getInputTypes().length == 1 ? operator.getOutputType() : (IValueType) ValueTypes.OPERATOR);
        this.operator = operator;
        this.listProxy = listProxy;
    }

    @Override
    public int getLength() throws EvaluationException {
        return listProxy.getLength();
    }

    @Override
    public IValue get(int index) throws EvaluationException {
        IValue value = listProxy.get(index);
        return ValueHelpers.evaluateOperator(operator, value);
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<IValueType<IValue>, IValue, ValueTypeListProxyOperatorMapped> {

        @Override
        public ResourceLocation getName() {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "mapped");
        }

        @Override
        protected void serializeNbt(ValueOutput valueOutput, ValueTypeListProxyOperatorMapped value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            Operators.REGISTRY.serialize(valueOutput.child("operator"), value.operator);
            ValueTypeListProxyFactories.REGISTRY.serialize(valueOutput.child("sublist"), value.listProxy);
        }

        @Override
        protected ValueTypeListProxyOperatorMapped deserializeNbt(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException, EvaluationException {
            IOperator operator = Operators.REGISTRY.deserialize(valueInput.child("operator").orElseThrow());
            IValueTypeListProxy<IValueType<IValue>, IValue> list = ValueTypeListProxyFactories.REGISTRY.deserialize(valueInput.child("sublist").orElseThrow());
            return new ValueTypeListProxyOperatorMapped(operator, list);
        }
    }

}
