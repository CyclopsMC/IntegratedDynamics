package org.cyclops.integrateddynamics.core.block;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/**
 * A block that can have its light level updated and stored.
 * @author rubensworks
 */
public interface IDynamicLightBlock {

    /**
     * Set the light level.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @param level The redstone level.
     */
    public void setLightLevel(IBlockAccess world, BlockPos pos, EnumFacing side, int level);

    /**
     * Get the light level.
     * @param world The world.
     * @param pos The position.
     * @param side The side.
     * @return The redstone level.
     */
    public int getLightLevel(IBlockAccess world, BlockPos pos, EnumFacing side);

}
