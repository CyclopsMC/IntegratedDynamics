package org.cyclops.integrateddynamics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
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

    public static final MapCodec<BlockDelay> CODEC = simpleCodec(BlockDelay::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockDelay(Properties properties) {
        super(properties, BlockEntityDelay::new);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_DELAY.get(), new BlockEntityDelay.Ticker());
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
                        if (itemStack.has(RegistryEntries.DATACOMPONENT_PROXY_ID)) {
                            tile.setProxyId(itemStack.get(RegistryEntries.DATACOMPONENT_PROXY_ID));
                        } else {
                            tile.generateNewProxyId();
                        }
                        tile.setChanged();
                    });
        }
        super.setPlacedBy(world, blockPos, state, placer, itemStack);
    }
}
