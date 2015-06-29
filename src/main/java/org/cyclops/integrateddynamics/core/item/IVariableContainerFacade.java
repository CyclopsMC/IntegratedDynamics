package org.cyclops.integrateddynamics.core.item;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Facade for a {@link IVariableContainer} at a certain position.
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
