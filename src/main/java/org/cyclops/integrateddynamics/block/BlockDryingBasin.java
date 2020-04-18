package org.cyclops.integrateddynamics.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.block.BlockTileGui;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.tileentity.TileDryingBasin;

/**
 * A block for drying stuff.
 * @author rubensworks
 */
public class BlockDryingBasin extends BlockTileGui {

    private static final VoxelShape SHAPE_RAYTRACE = makeCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.or(
            makeCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
            new VoxelShape[]{
                    makeCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
                    makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
                    SHAPE_RAYTRACE
            }), IBooleanFunction.ONLY_FIRST);

    public BlockDryingBasin(Properties properties) {
        super(properties, TileDryingBasin::new);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player,
                                             Hand hand, BlockRayTraceResult rayTraceResult) {
        return TileHelpers.getSafeTile(world, blockPos, TileDryingBasin.class)
                .map(tile -> {
                    ItemStack itemStack = player.inventory.getCurrentItem();
                    IFluidHandler itemFluidHandler = FluidUtil.getFluidHandler(itemStack).orElse(null);
                    SingleUseTank tank = tile.getTank();
                    ItemStack tileStack = tile.getInventory().getStackInSlot(0);

                    if (itemStack.isEmpty() && !tileStack.isEmpty()) {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, tileStack);
                        tile.getInventory().setInventorySlotContents(0, ItemStack.EMPTY);
                        tile.sendUpdate();
                        return ActionResultType.SUCCESS;
                    } else if(player.inventory.addItemStackToInventory(tileStack)){
                        tile.getInventory().setInventorySlotContents(0, ItemStack.EMPTY);
                        tile.sendUpdate();
                        return ActionResultType.SUCCESS;
                    } else if (itemFluidHandler != null && !tank.isFull()
                            && FluidUtil.tryEmptyContainer(itemStack, tank, Integer.MAX_VALUE, player, false).isSuccess()) {
                        ItemStack newItemStack = FluidUtil.tryEmptyContainer(itemStack, tank, Integer.MAX_VALUE, player, true).getResult();
                        InventoryHelpers.tryReAddToStack(player, itemStack, newItemStack);
                        tile.sendUpdate();
                        return ActionResultType.SUCCESS;
                    } else if (itemFluidHandler != null && !tank.isEmpty() &&
                            FluidUtil.tryFillContainer(itemStack, tank, Integer.MAX_VALUE, player, false).isSuccess()) {
                        ItemStack newItemStack = FluidUtil.tryFillContainer(itemStack, tank, Integer.MAX_VALUE, player, true).getResult();
                        InventoryHelpers.tryReAddToStack(player, itemStack, newItemStack);
                        return ActionResultType.SUCCESS;
                    } else if (!itemStack.isEmpty() && tileStack.isEmpty()) {
                        tile.getInventory().setInventorySlotContents(0, itemStack.split(1));
                        if(itemStack.getCount() <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                        tile.sendUpdate();
                        return ActionResultType.SUCCESS;
                    }
                    return ActionResultType.PASS;
                })
                .orElse(ActionResultType.PASS);
    }

    @Override
    public boolean isNormalCube(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(BlockState blockState) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos blockPos) {
        return TileHelpers.getSafeTile(world, blockPos, TileDryingBasin.class)
                .map(tile -> tile.getInventory().getStackInSlot(0) != null ? 15 : 0)
                .orElse(0);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
        return SHAPE;
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        return SHAPE_RAYTRACE;
    }
}
