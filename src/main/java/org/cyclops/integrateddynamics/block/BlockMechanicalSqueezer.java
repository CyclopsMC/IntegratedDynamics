package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidUtil;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.core.block.BlockMechanicalMachine;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalSqueezer;

/**
 * A block that can expose variables.
 * @author rubensworks
 */
public class BlockMechanicalSqueezer extends BlockMechanicalMachine {

    public static final String NBT_TANK = "tank";

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BlockMechanicalSqueezer(Properties properties) {
        super(properties, TileMechanicalSqueezer::new);

        this.setDefaultState(this.stateContainer.getBaseState()
                .with(LIT, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (FluidUtil.interactWithFluidHandler(player, hand, world, blockPos, rayTraceResult.getFace())) {
            return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(blockState, world, blockPos, player, hand, rayTraceResult);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isRemote()) {
            TileHelpers.getSafeTile(world, blockPos, TileMechanicalSqueezer.class)
                    .ifPresent(tile -> {
                        if (itemStack.hasTag() && itemStack.getTag().contains(NBT_TANK, Constants.NBT.TAG_COMPOUND)) {
                            tile.getTank().readFromNBT(itemStack.getTag().getCompound(NBT_TANK));
                        }
                    });
        }
        super.onBlockPlacedBy(world, blockPos, state, placer, itemStack);
    }

    @Override
    protected boolean isPickBlockPersistData() {
        return true;
    }
}
