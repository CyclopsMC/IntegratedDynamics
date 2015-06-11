package org.cyclops.integrateddynamics.core.part.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;

/**
 * A value holder for an {@link org.cyclops.integrateddynamics.core.part.read.IPartTypeReader}.
 * This is what will be serialized from and to NBT.
 * This object is mutable and should not be recreated.
 * @author rubensworks
 */
public interface IPartStateReader<P extends IPartTypeReader> extends IPartState<P> {

    /**
     * Get the singleton variable for an aspect.
     * This only retrieves the previously stored state.
     * Better to call {@link org.cyclops.integrateddynamics.core.part.read.IPartTypeReader#getVariable(org.cyclops.integrateddynamics.core.part.PartTarget, org.cyclops.integrateddynamics.core.part.read.IPartStateReader, org.cyclops.integrateddynamics.core.part.aspect.IAspectRead)}.
     * @param aspect The aspect from the part of this state.
     * @return The variable that exists only once for an aspect in this part state.
     */
    public <V extends IValue, T extends IValueType<V>> IAspectVariable<V> getVariable(IAspectRead<V, T> aspect);

    /**
     * Get the singleton variable for an aspect.
     * @param aspect The aspect from the part of this state.
     * @param variable The variable that exists only once for an aspect in this part state.
     */
    public void setVariable(IAspect aspect, IAspectVariable variable);

}
