package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.core.block.BlockTileGuiCabled;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * A block that can expose variables.
 * @author rubensworks
 */
public class BlockProxy extends BlockTileGuiCabled {

    public static final String NBT_ID = "proxyId";

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockProxy(Properties properties) {
        super(properties, TileProxy::new);
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(FACING, Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isRemote()) {
            TileHelpers.getSafeTile(world, blockPos, TileProxy.class)
                    .ifPresent(tile -> {
                        if (itemStack.hasTag() && itemStack.getTag().contains(NBT_ID, Constants.NBT.TAG_INT)) {
                            tile.setProxyId(itemStack.getTag().getInt(NBT_ID));
                        } else {
                            tile.generateNewProxyId();
                        }
                        tile.markDirty();
                    });
        }
        super.onBlockPlacedBy(world, blockPos, state, placer, itemStack);
    }
}
