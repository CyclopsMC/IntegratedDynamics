package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
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
        protected void serializeNbt(ValueDeseralizationContext valueDeseralizationContext, ValueTypeListProxyOperatorMapped value, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            tag.put("operator", Operators.REGISTRY.serialize(valueDeseralizationContext, value.operator));
            tag.put("sublist", ValueTypeListProxyFactories.REGISTRY.serialize(valueDeseralizationContext, value.listProxy));
        }

        @Override
        protected ValueTypeListProxyOperatorMapped deserializeNbt(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException, EvaluationException {
            IOperator operator = Operators.REGISTRY.deserialize(valueDeseralizationContext, tag.get("operator"));
            IValueTypeListProxy<IValueType<IValue>, IValue> list = ValueTypeListProxyFactories.REGISTRY.deserialize(valueDeseralizationContext, tag.get("sublist"));
            return new ValueTypeListProxyOperatorMapped(operator, list);
        }
    }

}
