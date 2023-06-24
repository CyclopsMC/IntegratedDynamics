package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.cyclops.cyclopscore.block.BlockWithEntity;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntitySqueezer;

import javax.annotation.Nullable;

/**
 * A block for squeezing stuff.
 * @author rubensworks
 */
public class BlockSqueezer extends BlockWithEntity {

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
            box(14.0F, 0.0F, 0.0F, 16F, 16.0F, 2F),
            box(0.0F, 0.0F, 14.0F, 2F, 16.0F, 16F),
            box(14.0F, 0.0F, 14.0F, 16F, 16.0F, 16F),
    };
    private static final VoxelShape[] SHAPES = {
            null,
            Shapes.or(SHAPES_BLOCK[1], SHAPES_STICKS),
            Shapes.or(SHAPES_BLOCK[2], SHAPES_STICKS),
            Shapes.or(SHAPES_BLOCK[3], SHAPES_STICKS),
            Shapes.or(SHAPES_BLOCK[4], SHAPES_STICKS),
            Shapes.or(SHAPES_BLOCK[5], SHAPES_STICKS),
            Shapes.or(SHAPES_BLOCK[6], SHAPES_STICKS),
            Shapes.or(SHAPES_BLOCK[7], SHAPES_STICKS),
    };

    public BlockSqueezer(Properties properties) {
        super(properties, BlockEntitySqueezer::new);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(HEIGHT, 1));
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_SQUEEZER, new BlockEntitySqueezer.Ticker());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, HEIGHT);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult blockRayTraceResult) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else if(world.getBlockState(blockPos).getValue(BlockSqueezer.HEIGHT) == 1) {
            return BlockEntityHelpers.get(world, blockPos, BlockEntitySqueezer.class)
                    .map(tile -> {
                        ItemStack itemStack = player.getInventory().getSelected();
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
                        } else if (!itemStack.isEmpty() && tile.getInventory().getItem(0).isEmpty()) {
                            tile.getInventory().setItem(0, itemStack.split(1));
                            if (itemStack.getCount() <= 0)
                                player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                            tile.sendUpdate();
                            return InteractionResult.SUCCESS;
                        }
                        return InteractionResult.PASS;
                    })
                    .orElse(InteractionResult.PASS);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        double motionY = entityIn.getDeltaMovement().y;
        super.updateEntityAfterFallOn(worldIn, entityIn);
        if(!entityIn.level().isClientSide() && motionY <= -0.37D && entityIn instanceof LivingEntity) {
            // Same way of deriving blockPos as is done in Entity#moveEntity
            int i = Mth.floor(entityIn.getX());
            int j = Mth.floor(entityIn.getY() - 0.2D);
            int k = Mth.floor(entityIn.getZ());
            BlockPos blockPos = new BlockPos(i, j, k);
            BlockState blockState = worldIn.getBlockState(blockPos);

            // The faster the entity is falling, the more steps to advance by
            int steps = 1 + Mth.floor((-motionY - 0.37D) * 5);

            if((entityIn.getY() - blockPos.getY()) - getRelativeTopPositionTop(worldIn, blockPos, blockState) <= 0.1F) {
                if (blockState.getBlock() == this) { // Just to be sure...
                    int newHeight = Math.min(7, blockState.getValue(HEIGHT) + steps);
                    entityIn.level().setBlockAndUpdate(blockPos, blockState.setValue(HEIGHT, newHeight));
                    BlockEntityHelpers.get(worldIn, blockPos, BlockEntitySqueezer.class)
                            .ifPresent(tile -> tile.setItemHeight(Math.max(newHeight, tile.getItemHeight())));
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, neighborBlock, fromPos, isMoving);
        if(!worldIn.isClientSide) {
            for (Direction enumfacing : Direction.values()) {
                if (worldIn.hasSignal(pos.relative(enumfacing), enumfacing)) {
                    worldIn.setBlockAndUpdate(pos, state.setValue(HEIGHT, 1));
                    for(Entity entity : worldIn.getEntitiesOfClass(Entity.class, new AABB(pos, pos.offset(1, 1, 1)))) {
                        entity.getDeltaMovement().add(0, 0.25F, 0);
                        entity.setDeltaMovement(0, 1, 0);
                    }
                    return;
                }
            }
        }
    }

    public float getRelativeTopPositionTop(BlockGetter world, BlockPos blockPos, BlockState blockState) {
        return (9 - blockState.getValue(HEIGHT)) * 0.125F;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext) {
        return SHAPES[blockState.getValue(HEIGHT)];
    }

    @Override
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter world, BlockPos blockPos) {
        return SHAPES_BLOCK[blockState.getValue(HEIGHT)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter world, BlockPos blockPos, CollisionContext selectionContext) {
        return SHAPES_BLOCK[blockState.getValue(HEIGHT)];
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos blockPos) {
        return (int) (((double) blockState.getValue(HEIGHT) - 1) / 6D * 15D);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntityHelpers.get(level, blockPos, BlockEntitySqueezer.class)
                    .ifPresent(tile -> {
                        InventoryHelpers.dropItems(level, tile.getInventory(), blockPos);
                        level.updateNeighbourForOutputSignal(blockPos, oldState.getBlock());
                    });
            super.onRemove(oldState, level, blockPos, newState, isMoving);
        }
    }
}
