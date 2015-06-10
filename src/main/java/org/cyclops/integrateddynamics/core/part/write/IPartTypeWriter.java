package org.cyclops.integrateddynamics.core.part.write;

import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;

import java.util.Set;

/**
 * A part type for writers.
 * @author rubensworks
 */
public interface IPartTypeWriter<P extends IPartType<P, S>, S extends IPartState<P>> extends IPartType<P, S> {

    /**
     * @return All possible write aspects that can be used in this part type.
     */
    public Set<IAspectWrite> getWriteAspects();

}
