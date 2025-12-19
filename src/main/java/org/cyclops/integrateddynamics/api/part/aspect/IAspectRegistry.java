package org.cyclops.integrateddynamics.api.part.aspect;

import net.minecraft.resources.Identifier;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.item.IAspectVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.api.part.IPartType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Registry for {@link IAspect}.
 * @author rubensworks
 */
public interface IAspectRegistry extends IRegistry, IVariableFacadeHandler<IAspectVariableFacade> {

    public IAspectRegistryClient getClient();

    /**
     * Register a new aspect for a given part type.
     * @param partType The part type.
     * @param aspect The aspect.
     * @return The registered element.
     */
    public IAspect register(IPartType partType, IAspect aspect);

    /**
     * Register a set of aspects for a given part type.
     * @param partType The part type.
     * @param aspects The aspects.
     */
    public void register(IPartType partType, Collection<IAspect> aspects);

    /**
     * Get the registered aspects for a given part type.
     * @param partType The part type.
     * @return The aspects.
     */
    public Set<IAspect> getAspects(IPartType partType);

    /**
     * Get the registered read aspects for a given part type.
     * @param partType The part type.
     * @return The read aspects.
     */
    public List<IAspectRead<?, ?>> getReadAspects(IPartType partType);

    /**
     * Get the registered write aspects for a given part type.
     * @param partType The part type.
     * @return The write aspects.
     */
    public List<IAspectWrite<?, ?>> getWriteAspects(IPartType partType);

    /**
     * Get all registered aspects.
     * @return The aspects.
     */
    public Set<IAspect> getAspects();

    /**
     * Get all registered read aspects.
     * @return The read aspects.
     */
    public Set<IAspectRead> getReadAspects();

    /**
     * Get all registered write aspects.
     * @return The write aspects.
     */
    public Set<IAspectWrite> getWriteAspects();

    /**
     * Get an aspect by name.
     * @param name The name of the aspect.
     * @return The matching aspect.
     */
    public IAspect getAspect(Identifier name);

}
