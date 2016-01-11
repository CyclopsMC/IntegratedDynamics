package org.cyclops.integrateddynamics.api.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

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

    /**
     * Get the side the player is watching.
     * @param world The world.
     * @param pos The block position to perform a ray trace for.
     * @param player The player.
     * @return The side the player is watching or null.
     */
    public @Nullable EnumFacing getWatchingSide(World world, BlockPos pos, EntityPlayer player);

}
