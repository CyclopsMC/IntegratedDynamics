package org.cyclops.integrateddynamics.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.BlockTileGui;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;

import java.util.Collection;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock.Properties;

/**
 * A base block with a gui and part entity that can connect to cables.
 * @author rubensworks
 */
public abstract class BlockTileGuiCabled extends BlockTileGui {

    public BlockTileGuiCabled(Properties properties, Supplier<CyclopsTileEntity> tileEntitySupplier) {
        super(properties, tileEntitySupplier);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player,
                                             Hand hand, BlockRayTraceResult rayTraceResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!world.isClientSide() && WrenchHelpers.isWrench(player, heldItem, world, blockPos, rayTraceResult.getDirection())
                && player.isSecondaryUseActive()) {
            Block.dropResources(blockState, world, blockPos, blockState.hasTileEntity() ? world.getBlockEntity(blockPos) : null, player, heldItem);
            world.destroyBlock(blockPos, false);
            return ActionResultType.SUCCESS;
        }
        return super.use(blockState, world, blockPos, player, hand, rayTraceResult);
    }

    @Override
    public void onPlace(BlockState blockState, World world, BlockPos blockPos, BlockState oldState, boolean isMoving) {
        super.onPlace(blockState, world, blockPos, oldState, isMoving);
        if (!world.isClientSide()) {
            CableHelpers.onCableAdded(world, blockPos);
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide()) {
            CableHelpers.onCableAddedByPlayer(world, pos, placer);
        }
    }

    @Override
    public void destroy(IWorld world, BlockPos blockPos, BlockState blockState) {
        CableHelpers.onCableRemoving((World) world, blockPos, true, false);
        Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables((World) world, blockPos);
        super.destroy(world, blockPos, blockState);
        CableHelpers.onCableRemoved((World) world, blockPos, connectedCables);
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos blockPos, Explosion explosion) {
        CableHelpers.setRemovingCable(true);
        CableHelpers.onCableRemoving(world, blockPos, true, false);
        Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables(world, blockPos);
        super.onBlockExploded(state, world, blockPos, explosion);
        CableHelpers.onCableRemoved(world, blockPos, connectedCables);
        CableHelpers.setRemovingCable(false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, isMoving);
        NetworkHelpers.onElementProviderBlockNeighborChange(world, pos, neighborBlock, null, fromPos);
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, world, pos, neighbor);
        if (world instanceof World) {
            NetworkHelpers.onElementProviderBlockNeighborChange((World) world, pos, world.getBlockState(neighbor).getBlock(), null, neighbor);
        }
    }

    @Override
    public void observedNeighborChange(BlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos) {
        super.observedNeighborChange(observerState, world, observerPos, changedBlock, changedBlockPos);
        NetworkHelpers.onElementProviderBlockNeighborChange(world, observerPos, changedBlock, null, changedBlockPos);
    }

    @Override
    public void onRemove(BlockState oldState, World world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            TileHelpers.getSafeTile(world, blockPos, TileCableConnectableInventory.class)
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
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos blockPos, PlayerEntity player) {
        if (isPickBlockPersistData()) {
            return super.getPickBlock(state, target, world, blockPos, player);
        } else {
            return getBlock().getCloneItemStack(world, blockPos, state);
        }
    }

}
