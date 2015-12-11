package org.cyclops.integrateddynamics.core.part.aspect;

import lombok.Data;
import lombok.NonNull;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;

/**
 * Variable for a specific aspect from a part that requires updates for value changes.
 * @author rubensworks
 */
@Data
public abstract class UpdatingAspectVariable<V extends IValue> implements IAspectVariable<V> {

    private final IValueType<V> type;
    private final PartTarget target;
    @NonNull private V value;

    public UpdatingAspectVariable(IValueType<V> type, PartTarget target) {
        this.type = type;
        this.target = target;
        this.value = type.getDefault();
    }

    @Override
    public boolean requiresUpdate() {
        return true;
    }

}
