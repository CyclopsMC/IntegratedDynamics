package org.cyclops.integrateddynamics.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.HitResult;
import org.cyclops.cyclopscore.block.BlockWithEntityGui;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectableInventory;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * A base block with a gui and part entity that can connect to cables.
 * @author rubensworks
 */
public abstract class BlockWithEntityGuiCabled extends BlockWithEntityGui {

    public BlockWithEntityGuiCabled(Properties properties, BiFunction<BlockPos, BlockState, CyclopsBlockEntity> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level world, BlockPos blockPos, Player player, BlockHitResult rayTraceResult) {
        ItemStack heldItem = player.getItemInHand(player.getUsedItemHand());
        if (WrenchHelpers.isWrench(player, heldItem, world, blockPos, rayTraceResult.getDirection())
                && player.isSecondaryUseActive()) {
            if (!world.isClientSide()) {
                Block.dropResources(blockState, world, blockPos, blockState.hasBlockEntity() ? world.getBlockEntity(blockPos) : null, player, heldItem);
                world.destroyBlock(blockPos, false);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(blockState, world, blockPos, player, rayTraceResult);
    }

    @Override
    public void onPlace(BlockState blockState, Level world, BlockPos blockPos, BlockState oldState, boolean isMoving) {
        super.onPlace(blockState, world, blockPos, oldState, isMoving);
        if (!world.isClientSide()) {
            CableHelpers.onCableAdded(world, blockPos);
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
            BlockEntityHelpers.get(world, blockPos, BlockEntityCableConnectableInventory.class)
                    .ifPresent(tile -> InventoryHelpers.dropItems(world, tile.getInventory(), blockPos));
            Collection<Direction> connectedCables = null;
            if (!CableHelpers.isRemovingCable()) {
                CableHelpers.onCableRemoving(world, blockPos, true, false);
                connectedCables = CableHelpers.getExternallyConnectedCables(world, blockPos);
            }
            super.onRemove(oldState, world, blockPos, newState, isMoving);
            if (!CableHelpers.isRemovingCable()) {
                CableHelpers.onCableRemoved(world, blockPos, connectedCables);
            }
        }
    }

    protected boolean isPickBlockPersistData() {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos blockPos, Player player) {
        if (isPickBlockPersistData()) {
            return super.getCloneItemStack(state, target, world, blockPos, player);
        } else {
            return getCloneItemStack(world, blockPos, state);
        }
    }

}
