package org.cyclops.integrateddynamics.core.block.cable;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.core.path.IPathElement;

/**
 * Interface for cables that can have parts.
 * @author rubensworks
 */
public interface ICableWithParts<E extends IPathElement<E>> extends ICable<E> {

    /**
     * Check if this cable has a part on the given side.
     * @param world The world.
     * @param pos The position of this block.
     * @param side The side to check a part for.
     * @return If this block has a part on the given side.
     */
    public boolean hasPart(World world, BlockPos pos, EnumFacing side);

}
