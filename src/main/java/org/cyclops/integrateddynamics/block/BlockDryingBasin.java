package org.cyclops.integrateddynamics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.block.BlockWithEntityGui;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;

import javax.annotation.Nullable;

/**
 * A block for drying stuff.
 * @author rubensworks
 */
public class BlockDryingBasin extends BlockWithEntityGui {

    public static final MapCodec<BlockDryingBasin> CODEC = simpleCodec(BlockDryingBasin::new);
    private static final VoxelShape SHAPE_RAYTRACE = box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(
            box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
            new VoxelShape[]{
                    box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
                    box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
                    SHAPE_RAYTRACE
            }), BooleanOp.ONLY_FIRST);

    public BlockDryingBasin(Properties properties) {
        super(properties, BlockEntityDryingBasin::new);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_DRYING_BASIN.get(), level.isClientSide ? new BlockEntityDryingBasin.TickerClient() : new BlockEntityDryingBasin.TickerServer());
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level world, BlockPos blockPos, Player player,
                                             BlockHitResult rayTraceResult) {
        return BlockEntityHelpers.get(world, blockPos, BlockEntityDryingBasin.class)
                .map(tile -> {
                    ItemStack itemStack = player.getInventory().getSelected();
                    IFluidHandler itemFluidHandler = FluidUtil.getFluidHandler(itemStack).orElse(null);
                    SingleUseTank tank = tile.getTank();
                    ItemStack tileStack = tile.getInventory().getItem(0);

                    if (itemStack.isEmpty() && !tileStack.isEmpty()) {
                        player.getInventory().setItem(player.getInventory().selected, tileStack);
                        tile.getInventory().setItem(0, ItemStack.EMPTY);
                        tile.sendUpdate();
                        return InteractionResult.SUCCESS;
                    } else if(player.getInventory().add(tileStack)){
                        tile.getInventory().setItem(0, ItemStack.EMPTY);
                        tile.sendUpdate();
                        return InteractionResult.SUCCESS;
                    } else if (itemFluidHandler != null && !tank.isFull()
                            && !itemFluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE).isEmpty()) {
                        FluidActionResult fluidAction = FluidUtil.tryEmptyContainer(itemStack, tank, Integer.MAX_VALUE, player, true);
                        if (fluidAction.isSuccess()) {
                            ItemStack newItemStack = fluidAction.getResult();
                            InventoryHelpers.tryReAddToStack(player, itemStack, newItemStack);
                            tile.sendUpdate();
                        }
                        return InteractionResult.SUCCESS;
                    } else if (itemFluidHandler != null && !tank.isEmpty() &&
                            itemFluidHandler.fill(tank.getFluid(), IFluidHandler.FluidAction.SIMULATE) > 0) {
                        FluidActionResult fluidAction = FluidUtil.tryFillContainer(itemStack, tank, Integer.MAX_VALUE, player, true);
                        if (fluidAction.isSuccess()) {
                            ItemStack newItemStack = fluidAction.getResult();
                            InventoryHelpers.tryReAddToStack(player, itemStack, newItemStack);
                        }
                        return InteractionResult.SUCCESS;
                    } else if (!itemStack.isEmpty() && tileStack.isEmpty()) {
                        tile.getInventory().setItem(0, itemStack.split(1));
                        if(itemStack.getCount() <= 0) player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                        tile.sendUpdate();
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.PASS;
                })
                .orElse(InteractionResult.PASS);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos blockPos) {
        return BlockEntityHelpers.get(world, blockPos, BlockEntityDryingBasin.class)
                .map(tile -> tile.getInventory().getItem(0) != null ? 15 : 0)
                .orElse(0);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter world, BlockPos blockPos) {
        return SHAPE_RAYTRACE;
    }

    @Override
    public void onRemove(BlockState oldState, Level world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            BlockEntityHelpers.get(world, blockPos, BlockEntityDryingBasin.class)
                    .ifPresent(tile -> {
                        InventoryHelpers.dropItems(world, tile.getInventory(), blockPos);
                        world.updateNeighbourForOutputSignal(blockPos, oldState.getBlock());
                    });
            super.onRemove(oldState, world, blockPos, newState, isMoving);
        }
    }
}
