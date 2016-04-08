package org.cyclops.integrateddynamics.api.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * A block that can have its redstone level updated and stored.
 * @author rubensworks
 */
public interface IDynamicRedstoneBlock {

    /**
     * Indicate that the given target must not provide any redstone level.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     */
    public void disableRedstoneAt(IBlockAccess world, BlockPos pos, EnumFacing side);

    /**
     * Set the redstone level.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @param level The redstone level.
     */
    public void setRedstoneLevel(IBlockAccess world, BlockPos pos, EnumFacing side, int level);

    /**
     * Get the redstone level.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The redstone level.
     */
    public int getRedstoneLevel(IBlockAccess world, BlockPos pos, EnumFacing side);

    /**
     * Set if this side allows redstone to be inputted.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @param allow If it allows input.
     */
    public void setAllowRedstoneInput(IBlockAccess world, BlockPos pos, EnumFacing side, boolean allow);

    /**
     * If this side allows redstone to be inputted.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return If it allows input.
     */
    public boolean isAllowRedstoneInput(IBlockAccess world, BlockPos pos, EnumFacing side);

}
