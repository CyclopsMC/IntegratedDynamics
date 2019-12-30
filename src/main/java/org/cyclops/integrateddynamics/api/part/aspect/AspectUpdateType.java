package org.cyclops.integrateddynamics.api.part.aspect;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
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
     * i.e., if {@link net.minecraft.block.Block#onNeighborChange(BlockState, IWorldReader, BlockPos, BlockPos)} or
     * {@link Block#neighborChanged(BlockState, World, BlockPos, Block, BlockPos, boolean)} is called.
     */
    BLOCK_UPDATE,
    /**
     * If the update method should never be called.
     */
    NEVER
}
