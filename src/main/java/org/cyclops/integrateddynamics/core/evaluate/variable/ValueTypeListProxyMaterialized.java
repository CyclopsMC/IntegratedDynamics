package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import java.util.List;

/**
 * A list proxy for a list that is fully materialized already.
 * @param <T> The value type type.
 * @param <V> The value type.
 */
public class ValueTypeListProxyMaterialized<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private final List<V> list;

    public ValueTypeListProxyMaterialized(T valueType, List<V> list) {
        super(ValueTypeListProxyFactories.MATERIALIZED.getName(), valueType);
        this.list = list;
    }

    @Override
    public int getLength() {
        return list.size();
    }

    @Override
    public V get(int index) {
        return list.get(index);
    }
}
