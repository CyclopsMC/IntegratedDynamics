package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
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

    private final ResourceLocation name;
    private final T valueType;

    public ValueTypeListProxyBase(ResourceLocation name, T valueType) {
        this.name = name;
        this.valueType = valueType;
    }

    @Override
    public T getValueType() {
        return valueType;
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }

    @Override
    public ITextComponent toCompactString() {
        StringTextComponent sb = new StringTextComponent("");
        boolean first = true;
        sb.appendText("[");
        for(V value : this) {
            if(!first) {
                sb.appendText(", ");
            }
            first = false;
            sb.appendSibling(value.getType().toCompactString(value));
            if(sb.getUnformattedComponentText().length() > 10) {
                sb.appendText("...");
                break;
            }
        }
        sb.appendText("]");
        return sb;
    }

    @Override
    public Iterator<V> iterator() {
        return new ValueTypeList.ListFactoryIterator<>(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;

        if(obj == null || !(obj instanceof ValueTypeListProxyBase)) {
            return false;
        }
        ValueTypeListProxyBase<?, ?> other = (ValueTypeListProxyBase<?, ?>) obj;
        if(!ValueHelpers.correspondsTo(getValueType(), other.getValueType())) {
            return false;
        }
        // Avoid infinite iteration
        if (this.isInfinite() || other.isInfinite()) {
            return false;
        }
        // Quickly return if the lengths differ.
        try {
            if (getLength() != other.getLength()) {
                return false;
            }
        } catch (EvaluationException e) {
            return false;
        }

        Object[] o = Iterables.toArray(this, Object.class);
        Object[] o2 = Iterables.toArray(other, Object.class);
        return Arrays.equals(o, o2);
    }

    @Override
    public int hashCode() {
        if(this.isInfinite()) return System.identityHashCode(this);
        int result = 1;
        result = 37 * result + getName().hashCode();
        result = 37 * result + getValueType().hashCode();
        for(Object obj : this) {
            result = 37 * result + (obj != null ? obj.hashCode() : 0);
        }
        return result;
    }

    @Override
    public boolean isInfinite() {
        return false;
    }
}
