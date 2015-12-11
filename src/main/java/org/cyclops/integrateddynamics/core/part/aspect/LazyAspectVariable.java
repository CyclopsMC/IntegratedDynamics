package org.cyclops.integrateddynamics.core.part.aspect;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

/**
 * Variable for a specific aspect from a part that calculates its target value only maximum once per ticking interval.
 * No calculations will be done if the value of this variable is not called.
 * @author rubensworks
 */
public abstract class LazyAspectVariable<V extends IValue> implements IAspectVariable<V> {

    @Getter private final IValueType<V> type;
    @Getter private final PartTarget target;
    @Getter private final IAspectRead<V, ?> aspect;
    @NonNull private V value;
    private IAspectProperties cachedProperties = null;

    public LazyAspectVariable(IValueType<V> type, PartTarget target, IAspectRead<V, ?> aspect) {
        this.type = type;
        this.target = target;
        this.aspect = aspect;
    }

    @Override
    public boolean requiresUpdate() {
        return value != null;
    }

    @Override
    public void update() {
        value = null;
        cachedProperties = null;
    }

    @Override
    public V getValue() {
        if(value == null) {
            this.value = getValueLazy();
        }
        return this.value;
    }

    protected IAspectProperties getAspectProperties() {
        if(cachedProperties == null && getAspect().hasProperties()) {
            PartPos pos = getTarget().getCenter();
            Pair<IPartType, IPartState> partData = PartPos.getPartData(pos);
            if (partData != null) {
                cachedProperties = getAspect().getProperties(partData.getLeft(), getTarget(), partData.getRight());
            }
        }
        return cachedProperties;
    }

    /**
     * Calculate the current value for this variable.
     * It will only be called when required.
     * @return The current value of this variable.
     */
    public abstract V getValueLazy();

}
