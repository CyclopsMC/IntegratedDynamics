package org.cyclops.integrateddynamics.api.block.cable;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.cyclops.integrateddynamics.api.path.IPathElement;

/**
 * Interface for cables that can have parts.
 * Mostly, this will go together with an implementation of
 * {@link org.cyclops.integrateddynamics.api.part.IPartContainer}.
 * This is simply a convenience interface.
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
    public boolean hasPart(IBlockAccess world, BlockPos pos, EnumFacing side);

}
