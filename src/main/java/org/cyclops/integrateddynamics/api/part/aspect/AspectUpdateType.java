package org.cyclops.integrateddynamics.api.part.aspect;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Different types of aspect update triggers.
 * I.e., when {@link IAspect#update(org.cyclops.integrateddynamics.api.network.INetwork, IPartNetwork, IPartType, PartTarget, IPartState)} should be called.
 * @author rubensworks
 */
public enum AspectUpdateType {
    /**
     * Update per network tick.
     */
    NETWORK_TICK,
    /**
     * Update its value on block neigbour changes,
     * i.e., if {@link net.minecraft.block.Block#onNeighborChange(IBlockAccess, BlockPos, BlockPos)} or
     * {@link Block#onNeighborChange(IBlockAccess, BlockPos, BlockPos)} is called.
     */
    BLOCK_UPDATE,
    /**
     * If the update method should never be called.
     */
    NEVER
}
