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
            box(0.0F, 0.0F, 0.0F, 16.0F, 16F, 16.0F),
            box(0.0F, 0.0F, 0.0F, 16.0F, 14F, 16.0F),
            box(0.0F, 0.0F, 0.0F, 16.0F, 12F, 16.0F),
            box(0.0F, 0.0F, 0.0F, 16.0F, 10F, 16.0F),
            box(0.0F, 0.0F, 0.0F, 16.0F, 8F, 16.0F),
            box(0.0F, 0.0F, 0.0F, 16.0F, 6F, 16.0F),
            box(0.0F, 0.0F, 0.0F, 16.0F, 4F, 16.0F),
    };
    private static final VoxelShape[] SHAPES_STICKS = {
            box(0.0F, 0.0F, 0.0F, 2F, 16.0F, 2F),
            box(16.0F, 0.0F, 0.0F, 14F, 16.0F, 2F),
            box(0.0F, 0.0F, 16.0F, 2F, 16.0F, 14F),
            box(16.0F, 0.0F, 16.0F, 14F, 16.0F, 14F),
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

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(HEIGHT, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AXIS, HEIGHT);
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (world.isClientSide()) {
            return ActionResultType.SUCCESS;
        } else if(world.getBlockState(blockPos).getValue(BlockSqueezer.HEIGHT) == 1) {
            return TileHelpers.getSafeTile(world, blockPos, TileSqueezer.class)
                    .map(tile -> {
                        ItemStack itemStack = player.inventory.getSelected();
                        ItemStack tileStack = tile.getInventory().getItem(0);

                        if (itemStack.isEmpty() && !tileStack.isEmpty()) {
                            player.inventory.setItem(player.inventory.selected, tileStack);
                            tile.getInventory().setItem(0, ItemStack.EMPTY);
                            tile.sendUpdate();
                            return ActionResultType.SUCCESS;
                        } else if(player.inventory.add(tileStack)){
                            tile.getInventory().setItem(0, ItemStack.EMPTY);
                            tile.sendUpdate();
                            return ActionResultType.SUCCESS;
                        } else if (!itemStack.isEmpty() && tile.getInventory().getItem(0).isEmpty()) {
                            tile.getInventory().setItem(0, itemStack.split(1));
                            if (itemStack.getCount() <= 0)
                                player.inventory.setItem(player.inventory.selected, ItemStack.EMPTY);
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
    public void updateEntityAfterFallOn(IBlockReader worldIn, Entity entityIn) {
        double motionY = entityIn.getDeltaMovement().y;
        super.updateEntityAfterFallOn(worldIn, entityIn);
        if(!entityIn.getCommandSenderWorld().isClientSide() && motionY <= -0.37D && entityIn instanceof LivingEntity) {
            // Same way of deriving blockPos as is done in Entity#moveEntity
            int i = MathHelper.floor(entityIn.getX());
            int j = MathHelper.floor(entityIn.getY() - 0.2D);
            int k = MathHelper.floor(entityIn.getZ());
            BlockPos blockPos = new BlockPos(i, j, k);
            BlockState blockState = worldIn.getBlockState(blockPos);

            // The faster the entity is falling, the more steps to advance by
            int steps = 1 + MathHelper.floor((-motionY - 0.37D) * 5);

            if((entityIn.getY() - blockPos.getY()) - getRelativeTopPositionTop(worldIn, blockPos, blockState) <= 0.1F) {
                if (blockState.getBlock() == this) { // Just to be sure...
                    int newHeight = Math.min(7, blockState.getValue(HEIGHT) + steps);
                    entityIn.getCommandSenderWorld().setBlockAndUpdate(blockPos, blockState.setValue(HEIGHT, newHeight));
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
        if(!worldIn.isClientSide) {
            for (Direction enumfacing : Direction.values()) {
                if (worldIn.hasSignal(pos.relative(enumfacing), enumfacing)) {
                    worldIn.setBlockAndUpdate(pos, state.setValue(HEIGHT, 1));
                    for(Entity entity : worldIn.getEntitiesOfClass(Entity.class, new AxisAlignedBB(pos, pos.offset(1, 1, 1)))) {
                        entity.getDeltaMovement().add(0, 0.25F, 0);
                        entity.setDeltaMovement(0, 1, 0);
                    }
                    return;
                }
            }
        }
    }

    public float getRelativeTopPositionTop(IBlockReader world, BlockPos blockPos, BlockState blockState) {
        return (9 - blockState.getValue(HEIGHT)) * 0.125F;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
        return SHAPES[blockState.getValue(HEIGHT)];
    }

    @Override
    public VoxelShape getInteractionShape(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        return SHAPES_BLOCK[blockState.getValue(HEIGHT)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
        return SHAPES_BLOCK[blockState.getValue(HEIGHT)];
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos blockPos) {
        return (int) (((double) blockState.getValue(HEIGHT) - 1) / 6D * 15D);
    }

    @Override
    public void onPlace(BlockState oldState, World world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            TileHelpers.getSafeTile(world, blockPos, TileSqueezer.class)
                    .ifPresent(tile -> {
                        InventoryHelpers.dropItems(world, tile.getInventory(), blockPos);
                        world.updateNeighbourForOutputSignal(blockPos, oldState.getBlock());
                    });
            super.onPlace(oldState, world, blockPos, newState, isMoving);
        }
    }

}
