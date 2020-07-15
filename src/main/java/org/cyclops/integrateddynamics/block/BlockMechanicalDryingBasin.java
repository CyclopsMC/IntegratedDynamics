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
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidUtil;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.core.block.BlockMechanicalMachine;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalDryingBasin;

/**
 * A block that can expose variables.
 * @author rubensworks
 */
public class BlockMechanicalDryingBasin extends BlockMechanicalMachine {

    public static final String NBT_TANK_IN = "tankInput";
    public static final String NBT_TANK_OUT = "tankOutput";

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BlockMechanicalDryingBasin(Properties properties) {
        super(properties, TileMechanicalDryingBasin::new);

        this.setDefaultState(this.stateContainer.getBaseState()
                .with(LIT, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (FluidUtil.interactWithFluidHandler(player, hand, world, blockPos, Direction.UP)
                || FluidUtil.interactWithFluidHandler(player, hand, world, blockPos, Direction.DOWN)) {
            return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(blockState, world, blockPos, player, hand, rayTraceResult);
    }

    @Override
    public boolean isNormalCube(BlockState blockState, IBlockReader world, BlockPos blockPos) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isRemote()) {
            TileHelpers.getSafeTile(world, blockPos, TileMechanicalDryingBasin.class)
                    .ifPresent(tile -> {
                        if (itemStack.hasTag()) {
                            if (itemStack.getTag().contains(NBT_TANK_IN, Constants.NBT.TAG_COMPOUND)) {
                                tile.getTankInput().readFromNBT(itemStack.getTag().getCompound(NBT_TANK_IN));
                            }
                            if (itemStack.getTag().contains(NBT_TANK_OUT, Constants.NBT.TAG_COMPOUND)) {
                                tile.getTankOutput().readFromNBT(itemStack.getTag().getCompound(NBT_TANK_OUT));
                            }
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
