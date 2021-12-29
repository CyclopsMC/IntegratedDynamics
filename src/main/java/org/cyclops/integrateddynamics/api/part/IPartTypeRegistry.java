package org.cyclops.integrateddynamics.api.part;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.init.IRegistry;

import java.util.Collection;

/**
 * Registry for {@link IPartType}.
 * @author rubensworks
 */
public interface IPartTypeRegistry extends IRegistry {

    /**
     * Register a new part type.
     * @param partType The part type.
     * @param <P> The part type.
     * @param <S> The state type.
     * @return The registered part type.
     */
    public <P extends IPartType<P, S>, S extends IPartState<P>> P register(P partType);

    /**
     * @return All registered part types.
     */
    public Collection<IPartType> getPartTypes();

    /**
     * Get the part type by unique name.
     * @param partName The unique part type name.
     * @return The associated part type or null.
     */
    public IPartType getPartType(ResourceLocation partName);

}
