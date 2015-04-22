package org.cyclops.integrateddynamics.core.part;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Facade for a {@link org.cyclops.integrateddynamics.core.part.IPartContainer} at a certain position.
 * @author rubensworks
 */
public interface IPartContainerFacade {

    /**
     * Get the part container at a given position.
     * @param world The world.
     * @param pos The position.
     * @return The part container.
     */
    public IPartContainer getPartContainer(World world, BlockPos pos);

}
