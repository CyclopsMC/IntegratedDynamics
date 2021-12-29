package org.cyclops.integrateddynamics.api.part.aspect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
     * i.e., if {@link Block#neighborChanged(BlockState, Level, BlockPos, Block, BlockPos, boolean)},
     * {@link Block#onNeighborChange(BlockState, LevelReader, BlockPos, BlockPos)}
     * or {@link Block#updateShape(BlockState, Direction, BlockState, LevelAccessor, BlockPos, BlockPos)} is called.
     */
    BLOCK_UPDATE,
    /**
     * If the update method should never be called.
     */
    NEVER
}
