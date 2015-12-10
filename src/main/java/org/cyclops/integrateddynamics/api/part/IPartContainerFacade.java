package org.cyclops.integrateddynamics.api.part;

import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Facade for a {@link IPartContainer} at a certain position.
 * Must be implemented by a block.
 * @author rubensworks
 */
public interface IPartContainerFacade {

    /**
     * Get the part container at a given position.
     * @param world The world.
     * @param pos The position.
     * @return The part container.
     */
    public IPartContainer getPartContainer(IBlockAccess world, BlockPos pos);

}
