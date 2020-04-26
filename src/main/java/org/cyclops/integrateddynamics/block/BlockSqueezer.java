package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.BlockTile;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;

/**
 * A block for squeezing stuff.
 * @author rubensworks
 */
public class BlockSqueezer extends BlockTile {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 1, 7); // 1 is heighest, 7 is lowest

    private static final VoxelShape[] SHAPES_BLOCK = {
            null,
            makeCuboidShape(0.0F, 0.0F, 0.0F, 16.0F, 16F, 16.0F),
            makeCuboidShape(0.0F, 0.0F, 0.0F, 16.0F, 14F, 16.0F),
            makeCuboidShape(0.0F, 0.0F, 0.0F, 16.0F, 12F, 16.0F),
            makeCuboidShape(0.0F, 0.0F, 0.0F, 16.0F, 105, 16.0F),
            makeCuboidShape(0.0F, 0.0F, 0.0F, 16.0F, 8F, 16.0F),
            makeCuboidShape(0.0F, 0.0F, 0.0F, 16.0F, 6F, 16.0F),
            makeCuboidShape(0.0F, 0.0F, 0.0F, 16.0F, 4F, 16.0F),
    };
    private static final VoxelShape[] SHAPES_STICKS = {
            makeCuboidShape(0.0F, 0.0F, 0.0F, 2F, 16.0F, 2F),
            makeCuboidShape(16.0F, 0.0F, 0.0F, 14F, 16.0F, 2F),
            makeCuboidShape(0.0F, 0.0F, 16.0F, 2F, 16.0F, 14F),
            makeCuboidShape(16.0F, 0.0F, 16.0F, 14F, 16.0F, 14F),
    };
    private static final VoxelShape[] SHAPES = {
            null,
            VoxelShapes.or(SHAPES_BLOCK[1], SHAPES_STICKS),
            VoxelShapes.or(SHAPES_BLOCK[2], SHAPES_STICKS),
            VoxelShapes.or(SHAPES_BLOCK[3], SHAPES_STICKS),
            VoxelShapes.or(SHAPES_BLOCK[4], SHAPES_STICKS),
            VoxelShapes.or(SHAPES_BLOCK[5], SHAPES_STICKS),
            VoxelShapes.or(SHAPES_BLOCK[6], SHAPES_STICKS),
            VoxelShapes.or(SHAPES_BLOCK[7], SHAPES_STICKS),
    };

    public BlockSqueezer(Properties properties) {
        super(properties, TileSqueezer::new);

        this.setDefaultState(this.stateContainer.getBaseState()
                .with(AXIS, Direction.Axis.X)
                .with(HEIGHT, 1));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AXIS, HEIGHT);
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(AXIS, context.getPlacementHorizontalFacing().getAxis());
    }

    @Override
    public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (world.isRemote()) {
            return ActionResultType.SUCCESS;
        } else if(world.getBlockState(blockPos).get(BlockSqueezer.HEIGHT) == 1) {
            return TileHelpers.getSafeTile(world, blockPos, TileSqueezer.class)
                    .map(tile -> {
                        ItemStack itemStack = player.inventory.getCurrentItem();
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
                        } else if (!itemStack.isEmpty() && tile.getInventory().getStackInSlot(0).isEmpty()) {
                            tile.getInventory().setInventorySlotContents(0, itemStack.split(1));
                            if (itemStack.getCount() <= 0)
                                player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                            tile.sendUpdate();
                            return ActionResultType.SUCCESS;
                        }
                        return ActionResultType.PASS;
                    })
                    .orElse(ActionResultType.PASS);
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onLanded(IBlockReader worldIn, Entity entityIn) {
        double motionY = entityIn.getMotion().y;
        super.onLanded(worldIn, entityIn);
        if(!entityIn.getEntityWorld().isRemote() && motionY <= -0.37D && entityIn instanceof LivingEntity) {
            // Same way of deriving blockPos as is done in Entity#moveEntity
            int i = MathHelper.floor(entityIn.getPosX());
            int j = MathHelper.floor(entityIn.getPosY() - 0.2D);
            int k = MathHelper.floor(entityIn.getPosZ());
            BlockPos blockPos = new BlockPos(i, j, k);
            BlockState blockState = worldIn.getBlockState(blockPos);

            // The faster the entity is falling, the more steps to advance by
            int steps = 1 + MathHelper.floor((-motionY - 0.37D) * 5);

            if((entityIn.getPosY() - blockPos.getY()) - getRelativeTopPositionTop(worldIn, blockPos, blockState) <= 0.1F) {
                if (blockState.getBlock() == this) { // Just to be sure...
                    int newHeight = Math.min(7, blockState.get(HEIGHT) + steps);
                    entityIn.getEntityWorld().setBlockState(blockPos, blockState.with(HEIGHT, newHeight));
                    TileHelpers.getSafeTile(worldIn, blockPos, TileSqueezer.class)
                            .ifPresent(tile -> tile.setItemHeight(Math.max(newHeight, tile.getItemHeight())));
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, neighborBlock, fromPos, isMoving);
        if(!worldIn.isRemote) {
            for (Direction enumfacing : Direction.values()) {
                if (worldIn.isSidePowered(pos.offset(enumfacing), enumfacing)) {
                    worldIn.setBlockState(pos, state.with(HEIGHT, 1));
                    for(Entity entity : worldIn.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)))) {
                        entity.getMotion().add(0, 0.25F, 0);
                        entity.setMotion(0, 1, 0);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, Direction facing, BlockState state2, IWorld world, BlockPos pos1, BlockPos pos2, Hand hand) {
        return super.getStateForPlacement(state, facing, state2, world, pos1, pos2, hand)
                .with(AXIS, facing.getAxis());
    }

    public float getRelativeTopPositionTop(IBlockReader world, BlockPos blockPos, BlockState blockState) {
        return (9 - blockState.get(HEIGHT)) * 0.125F;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
        return SHAPES[blockState.get(HEIGHT)];
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        return SHAPES_BLOCK[blockState.get(HEIGHT)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
        return SHAPES_BLOCK[blockState.get(HEIGHT)];
    }

    @Override
    public boolean isNormalCube(BlockState blockState, IBlockReader world, BlockPos pos) {
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
        return (int) (((double) blockState.get(HEIGHT) - 1) / 6D * 15D);
    }

    @Override
    public void onReplaced(BlockState oldState, World world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            TileHelpers.getSafeTile(world, blockPos, TileSqueezer.class)
                    .ifPresent(tile -> {
                        InventoryHelpers.dropItems(world, tile.getInventory(), blockPos);
                        world.updateComparatorOutputLevel(blockPos, oldState.getBlock());
                    });
            super.onReplaced(oldState, world, blockPos, newState, isMoving);
        }
    }

}
