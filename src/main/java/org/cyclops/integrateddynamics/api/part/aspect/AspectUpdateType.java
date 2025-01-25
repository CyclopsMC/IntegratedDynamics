package org.cyclops.integrateddynamics.api.part.aspect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
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
     * i.e., if {@link Block#neighborChanged(BlockState, Level, BlockPos, Block, Orientation, boolean)} ,
     * {@link Block#onNeighborChange(BlockState, LevelReader, BlockPos, BlockPos)}
     * or {@link Block#updateShape(BlockState, LevelReader, ScheduledTickAccess, BlockPos, Direction, BlockPos, BlockState, RandomSource)}  is called.
     */
    BLOCK_UPDATE,
    /**
     * If the update method should never be called.
     */
    NEVER
}
