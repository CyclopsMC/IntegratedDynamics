package org.cyclops.integrateddynamics.core.path;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Classes that can create instances of an {@link org.cyclops.integrateddynamics.core.path.IPathElement}.
 * @author rubensworks
 */
public interface IPathElementProvider<E extends IPathElement<E>> {

    /**
     * Create a path element instance for the given position.
     * @param world The world.
     * @param blockPos The position.
     * @return The path element instance at this position.
     */
    public E createPathElement(World world, BlockPos blockPos);

}
