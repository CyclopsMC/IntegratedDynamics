package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDelay;
import org.cyclops.integrateddynamics.core.block.BlockWithEntityGuiCabled;

import javax.annotation.Nullable;

/**
 * A block that can delay variables.
 * @author rubensworks
 */
public class BlockDelay extends BlockWithEntityGuiCabled {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockDelay(Properties properties) {
        super(properties, BlockEntityDelay::new);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_DELAY, new BlockEntityDelay.Ticker());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide()) {
            BlockEntityHelpers.get(world, blockPos, BlockEntityDelay.class)
                    .ifPresent(tile -> {
                        if (itemStack.hasTag() && itemStack.getTag().contains(BlockProxy.NBT_ID, Tag.TAG_INT)) {
                            tile.setProxyId(itemStack.getTag().getInt(BlockProxy.NBT_ID));
                        } else {
                            tile.generateNewProxyId();
                        }
                        tile.setChanged();
                    });
        }
        super.setPlacedBy(world, blockPos, state, placer, itemStack);
    }
}
