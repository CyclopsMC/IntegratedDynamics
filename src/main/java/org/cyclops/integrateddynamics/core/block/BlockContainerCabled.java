package org.cyclops.integrateddynamics.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.cyclopscore.block.BlockWithEntity;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * A base block with part entity that can connect to cables.
 * @author rubensworks
 */
public abstract class BlockContainerCabled extends BlockWithEntity {

    public BlockContainerCabled(Block.Properties properties, BiFunction<BlockPos, BlockState, CyclopsBlockEntity> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                             BlockHitResult blockRayTraceResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!world.isClientSide() && WrenchHelpers.isWrench(player, heldItem, world, pos, blockRayTraceResult.getDirection()) && player.isSecondaryUseActive()) {
            world.destroyBlock(pos, true);
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, blockRayTraceResult);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        if (!world.isClientSide()) {
            CableHelpers.onCableAdded(world, pos);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide()) {
            CableHelpers.onCableAddedByPlayer(world, pos, placer);
        }
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos blockPos, BlockState blockState) {
        CableHelpers.onCableRemoving((Level) world, blockPos, true, false);
        Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables((Level) world, blockPos);
        super.destroy(world, blockPos, blockState);
        CableHelpers.onCableRemoved((Level) world, blockPos, connectedCables);
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos blockPos, Explosion explosion) {
        CableHelpers.setRemovingCable(true);
        CableHelpers.onCableRemoving(world, blockPos, true, false);
        Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables(world, blockPos);
        super.onBlockExploded(state, world, blockPos, explosion);
        CableHelpers.onCableRemoved(world, blockPos, connectedCables);
        CableHelpers.setRemovingCable(false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, isMoving);
        NetworkHelpers.onElementProviderBlockNeighborChange(world, pos, neighborBlock, null, fromPos);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, world, pos, neighbor);
        if (world instanceof Level) {
            NetworkHelpers.onElementProviderBlockNeighborChange((Level) world, pos, world.getBlockState(neighbor).getBlock(), null, neighbor);
        }
    }

    @Override
    public void onRemove(BlockState oldState, Level world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            Collection<Direction> connectedCables = null;
            if (!CableHelpers.isRemovingCable()) {
                CableHelpers.onCableRemoving(world, blockPos, true, false);
                connectedCables = CableHelpers.getExternallyConnectedCables(world, blockPos);
            }
            super.onRemove(oldState, world, blockPos, newState, isMoving);
            if (!CableHelpers.isRemovingCable()) {
                CableHelpers.onCableRemoved(world, blockPos, connectedCables);
            }
        } else {
            super.onRemove(oldState, world, blockPos, newState, isMoving);
        }
    }
}
