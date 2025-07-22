package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

import java.util.concurrent.TimeUnit;

/**
 * A list that is built lazily from a start value and an operator.
 * @param <T> The value type type.
 * @param <V> The value type.
 */
public class ValueTypeListProxyLazyBuilt<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private Cache<Integer, V> cache_values = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).maximumSize(100).build();

    private final V value;
    private final IOperator operator;

    public ValueTypeListProxyLazyBuilt(V value, IOperator operator) {
        super(ValueTypeListProxyFactories.LAZY_BUILT.getName(), (T) value.getType());
        this.value = value;
        this.operator = operator;
    }

    @Override
    public int getLength() throws EvaluationException {
        return Integer.MAX_VALUE;
    }

    @Override
    public V get(int index) throws EvaluationException {
        if (index == 0) {
            return value;
        }
        V current = cache_values.getIfPresent(index);
        if (current != null) {
            return current;
        }
        V previous = get(index - 1);
        current = (V) operator.evaluate(new IVariable[]{new Variable(previous.getType(), previous)});
        cache_values.put(index, current);
        return current;
    }

    @Override
    public boolean isInfinite() {
        return true;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<IValueType<IValue>, IValue, ValueTypeListProxyLazyBuilt<IValueType<IValue>, IValue>> {

        @Override
        public ResourceLocation getName() {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "lazybuilt");
        }

        @Override
        protected void serializeNbt(ValueOutput valueOutput, ValueTypeListProxyLazyBuilt<IValueType<IValue>, IValue> value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            valueOutput.putString("valueType", value.value.getType().getUniqueName().toString());
            ValueHelpers.serializeRaw(valueOutput.child("v"), value.value);
            Operators.REGISTRY.serialize(valueOutput.child("operator"), value.operator);
        }

        @Override
        protected ValueTypeListProxyLazyBuilt<IValueType<IValue>, IValue> deserializeNbt(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException, EvaluationException {
            IValueType valueType = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(valueInput.getString("valueType").orElseThrow()));
            IValue value = ValueHelpers.deserializeRaw(valueInput.child("v").orElseThrow(), valueType);
            IOperator operator = Operators.REGISTRY.deserialize(valueInput.child("operator").orElseThrow());
            return new ValueTypeListProxyLazyBuilt<>(value, operator);
        }
    }
}
