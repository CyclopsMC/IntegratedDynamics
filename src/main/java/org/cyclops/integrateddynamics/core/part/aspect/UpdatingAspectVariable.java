package org.cyclops.integrateddynamics.core.part.aspect;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;

import java.util.Objects;

/**
 * Variable for a specific aspect from a part that requires updates for value changes.
 * @author rubensworks
 */
public abstract class UpdatingAspectVariable<V extends IValue> implements IAspectVariable<V> {

    private final IValueType<V> type;
    private final PartTarget target;
    private V value;

    public UpdatingAspectVariable(IValueType<V> type, PartTarget target) {
        this.type = type;
        this.target = target;
        this.value = type.getDefault();
    }

    public IValueType<V> getType() {
        return type;
    }

    public PartTarget getTarget() {
        return target;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = Objects.requireNonNull(value, "value is marked non-null but is null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdatingAspectVariable<?> that = (UpdatingAspectVariable<?>) o;
        return Objects.equals(type, that.type) && Objects.equals(target, that.target) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, target, value);
    }

    @Override
    public String toString() {
        return "UpdatingAspectVariable{" +
                "type=" + type +
                ", target=" + target +
                ", value=" + value +
                '}';
    }

}
