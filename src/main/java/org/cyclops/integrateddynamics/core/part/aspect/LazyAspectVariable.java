package org.cyclops.integrateddynamics.core.part.aspect;

import lombok.Getter;
import lombok.NonNull;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.PartTarget;

/**
 * Variable for a specific aspect from a part that calculates its target value only maximum once per ticking interval.
 * No calculations will be done if the value of this variable is not called.
 * @author rubensworks
 */
public abstract class LazyAspectVariable<V extends IValue> implements IAspectVariable<V> {

    @Getter private final IValueType<V> type;
    @Getter private final PartTarget target;
    @NonNull private V value;

    public LazyAspectVariable(IValueType<V> type, PartTarget target) {
        this.type = type;
        this.target = target;
    }

    @Override
    public boolean requiresUpdate() {
        return value != null;
    }

    @Override
    public void update() {
        value = null;
    }

    @Override
    public V getValue() {
        if(value == null) {
            this.value = getValueLazy();
        }
        return this.value;
    }

    /**
     * Calculate the current value for this variable.
     * It will only be called when required.
     * @return The current value of this variable.
     */
    public abstract V getValueLazy();

}
