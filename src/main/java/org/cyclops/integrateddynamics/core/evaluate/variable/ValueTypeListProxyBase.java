package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A list proxy base implementation.
 * @param <T> The value type type.
 * @param <V> The value type.
 */
public abstract class ValueTypeListProxyBase<T extends IValueType<V>, V extends IValue> implements IValueTypeListProxy<T, V> {

    private final String name;
    private final T valueType;

    public ValueTypeListProxyBase(String name, T valueType) {
        this.name = name;
        this.valueType = valueType;
    }

    @Override
    public T getValueType() {
        return valueType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("[");
        for(V value : this) {
            if(!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(getValueType().toCompactString(value));
            if(sb.toString().length() > 100) {
                break;
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public Iterator<V> iterator() {
        return new ValueTypeList.ListFactoryIterator<>(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof ValueTypeListProxyBase)) {
            return false;
        }
        ValueTypeListProxyBase other = (ValueTypeListProxyBase) obj;
        if(!getName().equals(other.getName()) || !(getValueType() == other.getValueType())) {
            return false;
        }

        Object[] o = Iterables.toArray(this, Object.class);
        Object[] o2 = Iterables.toArray(other, Object.class);
        return Arrays.equals(o, o2);
    }
}
