package org.cyclops.integrateddynamics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalDryingBasin;
import org.cyclops.integrateddynamics.core.block.BlockMechanicalMachine;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMechanicalMachine;

import javax.annotation.Nullable;

/**
 * A block that can expose variables.
 * @author rubensworks
 */
public class BlockMechanicalDryingBasin extends BlockMechanicalMachine {

    public static final MapCodec<BlockMechanicalDryingBasin> CODEC = simpleCodec(BlockMechanicalDryingBasin::new);

    public static final String NBT_TANK_IN = "tankInput";
    public static final String NBT_TANK_OUT = "tankOutput";

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BlockMechanicalDryingBasin(Properties properties) {
        super(properties, BlockEntityMechanicalDryingBasin::new);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_MECHANICAL_DRYING_BASIN.get(), new BlockEntityMechanicalMachine.Ticker<>());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (FluidUtil.interactWithFluidHandler(player, hand, world, blockPos, Direction.UP)
                || FluidUtil.interactWithFluidHandler(player, hand, world, blockPos, Direction.DOWN)) {
            return InteractionResult.SUCCESS;
        }
        return super.use(blockState, world, blockPos, player, hand, rayTraceResult);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide()) {
            BlockEntityHelpers.get(world, blockPos, BlockEntityMechanicalDryingBasin.class)
                    .ifPresent(tile -> {
                        if (itemStack.hasTag()) {
                            if (itemStack.getTag().contains(NBT_TANK_IN, Tag.TAG_COMPOUND)) {
                                tile.getTankInput().readFromNBT(itemStack.getTag().getCompound(NBT_TANK_IN));
                            }
                            if (itemStack.getTag().contains(NBT_TANK_OUT, Tag.TAG_COMPOUND)) {
                                tile.getTankOutput().readFromNBT(itemStack.getTag().getCompound(NBT_TANK_OUT));
                            }
                        }
                    });
        }
        super.setPlacedBy(world, blockPos, state, placer, itemStack);
    }

    @Override
    protected boolean isPickBlockPersistData() {
        return true;
    }
}
