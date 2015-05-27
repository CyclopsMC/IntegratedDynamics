package org.cyclops.integrateddynamics.core.part.aspect;

import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.core.part.IPartType;

import java.util.Set;

/**
 * Registry for {@link org.cyclops.integrateddynamics.core.part.aspect.IAspect}.
 * @author rubensworks
 */
public interface IAspectRegistry extends IRegistry {

    /**
     * Register a new aspect for a given part type.
     * @param partType The part type.
     * @param aspect The aspect.
     */
    public void register(IPartType partType, IAspect aspect);

    /**
     * Register a set of aspects for a given part type.
     * @param partType The part type.
     * @param aspects The aspects.
     */
    public void register(IPartType partType, Set<IAspect> aspects);

    /**
     * Get the registered aspects for a given part type.
     * @param partType The part type.
     * @return The aspects.
     */
    public Set<IAspect> getAspects(IPartType partType);

}
