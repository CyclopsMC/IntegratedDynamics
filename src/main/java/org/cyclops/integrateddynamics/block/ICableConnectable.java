package org.cyclops.integrateddynamics.block;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

/**
 * Interface for blocks which can connect with cables.
 * @author rubensworks
 */
public interface ICableConnectable {

    /**
     * Check if the given position should connect with this.
     * @param world The world.
     * @param selfPosition The position for this block.
     * @param connector The connecting block.
     * @param otherPosition The position of the connecting block.
     * @return If it should connect.
     */
    public boolean canConnect(World world, BlockPos selfPosition, ICableConnectable connector, BlockPos otherPosition);

    /**
     * Update the cable connections at the given position.
     * @param world The world.
     * @param pos The position of this block.
     * @return The resulting state.
     */
    public IExtendedBlockState updateConnections(World world, BlockPos pos);

}
