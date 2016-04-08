package org.cyclops.integrateddynamics.api.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Facade for a {@link IVariableContainer} at a certain position.
 * Must be implemented on blocks.
 * @author rubensworks
 */
public interface IVariableContainerFacade {

    /**
     * Get the variable container at a given position.
     * @param world The world.
     * @param pos The position.
     * @return The variable container.
     */
    public IVariableContainer getVariableContainer(World world, BlockPos pos);

}
