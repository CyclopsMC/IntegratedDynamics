package org.cyclops.integrateddynamics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.cyclops.cyclopscore.block.BlockWithEntityGui;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.IFluidHelpersNeoForge;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
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
        return createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_DRYING_BASIN.get(), level.isClientSide() ? new BlockEntityDryingBasin.TickerClient() : new BlockEntityDryingBasin.TickerServer());
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level world, BlockPos blockPos, Player player,
                                             BlockHitResult rayTraceResult) {
        return IModHelpers.get().getBlockEntityHelpers().get(world, blockPos, BlockEntityDryingBasin.class)
                .map(tile -> {
                    IFluidHelpersNeoForge fh = IModHelpersNeoForge.get().getFluidHelpers();
                    ItemAccess itemAccess = ItemAccess.forPlayerSlot(player, player.getInventory().getSelectedSlot());
                    ResourceHandler<FluidResource> itemFluidHandler = itemAccess.getCapability(Capabilities.Fluid.ITEM);
                    SingleUseTank tank = tile.getTank();
                    ItemStack tileStack = tile.getInventory().getItem(0);

                    if (itemAccess.getAmount() == 0 && !tileStack.isEmpty()) {
                        player.getInventory().setItem(player.getInventory().getSelectedSlot(), tileStack);
                        tile.getInventory().setItem(0, ItemStack.EMPTY);
                        tile.sendUpdate();
                        return InteractionResult.SUCCESS;
                    } else if(player.getInventory().add(tileStack)){
                        tile.getInventory().setItem(0, ItemStack.EMPTY);
                        tile.sendUpdate();
                        return InteractionResult.SUCCESS;
                    } else if (itemFluidHandler != null && !tank.isFull() && fh.canExtract(itemFluidHandler)) {
                        fh.move(itemFluidHandler, tank, Integer.MAX_VALUE, player, true, false);
                        return InteractionResult.SUCCESS;
                    } else if (itemFluidHandler != null && !tank.isEmpty() && fh.canInsert(itemFluidHandler, tank.getFluid())) {
                        fh.move(tank, itemFluidHandler, Integer.MAX_VALUE, player, false, false);
                        return InteractionResult.SUCCESS;
                    } else if (itemAccess.getAmount() > 0 && tileStack.isEmpty()) {
                        tile.getInventory().setItem(0, itemAccess.getResource().toStack());
                        player.getInventory().getItem(player.getInventory().getSelectedSlot()).shrink(1);
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

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return IModHelpers.get().getBlockEntityHelpers().get(level, pos, BlockEntityDryingBasin.class)
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
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
}
