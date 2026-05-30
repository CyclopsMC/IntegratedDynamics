package org.cyclops.integrateddynamics.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.cyclopscore.block.BlockWithEntityGui;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.jetbrains.annotations.Nullable;

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
    public InteractionResult useItemOn(ItemStack pStack, BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand pHand, BlockHitResult rayTraceResult) {
        ItemStack heldItem = player.getItemInHand(player.getUsedItemHand());
        if (WrenchHelpers.isWrench(player, heldItem, world, blockPos, rayTraceResult.getDirection())
                && player.isSecondaryUseActive()) {
            if (!world.isClientSide()) {
                Block.dropResources(blockState, world, blockPos, blockState.hasBlockEntity() ? world.getBlockEntity(blockPos) : null, player, heldItem);
                world.destroyBlock(blockPos, false);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(pStack, blockState, world, blockPos, player, pHand, rayTraceResult);
    }

    @Override
    public void onPlace(BlockState blockState, Level world, BlockPos blockPos, BlockState oldState, boolean isMoving) {
        super.onPlace(blockState, world, blockPos, oldState, isMoving);
        if (!world.isClientSide() && oldState.getBlock() != this) {
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
        CableHelpers.onCableRemoving((Level) world, blockPos, true, false, blockState, world.getBlockEntity(blockPos));
        super.destroy(world, blockPos, blockState);
        CableHelpers.onCableRemoved((Level) world, blockPos);
    }

    @Override
    public void onBlockExploded(BlockState state, ServerLevel world, BlockPos blockPos, Explosion explosion) {
        CableHelpers.setRemovingCable(true);
        CableHelpers.onCableRemoving(world, blockPos, true, false, state, world.getBlockEntity(blockPos));
        super.onBlockExploded(state, world, blockPos, explosion);
        CableHelpers.onCableRemoved(world, blockPos);
        CableHelpers.setRemovingCable(false);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        NetworkHelpers.onElementProviderBlockNeighborChange(level, pos, null);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, world, pos, neighbor);
        if (world instanceof Level) {
            NetworkHelpers.onElementProviderBlockNeighborChange((Level) world, pos, null);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);

        if (!CableHelpers.isRemovingCable()) {
            CableHelpers.onCableRemoved(level, pos);
        }
    }

    protected boolean isPickBlockPersistData() {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        if (isPickBlockPersistData()) {
            return super.getCloneItemStack(level, pos, state, includeData, player);
        } else {
            return getCloneItemStack(level, pos, state, false);
        }
    }
}
