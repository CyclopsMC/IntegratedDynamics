package org.cyclops.integrateddynamics.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import net.minecraft.item.Item.Properties;

/**
 * The default wrench for this mod.
 * @author rubensworks
 */
public class ItemWrench extends Item {

    public ItemWrench(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return true;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive()) {
            return super.useOn(context);
        }
        if (context.getClickedFace().getAxis() == Direction.Axis.Y
                && blockState.hasProperty(BlockStateProperties.FACING)) {
            // If pointing top or bottom, and we can rotate to UP and DOWN, rotate to that direction or opposite
            blockState = blockState.setValue(BlockStateProperties.FACING, blockState.getValue(BlockStateProperties.FACING) == Direction.UP ? Direction.DOWN : Direction.UP);
        } else if (context.getClickedFace().getAxis() != Direction.Axis.Y
                && blockState.hasProperty(BlockStateProperties.FACING)
                && blockState.getValue(BlockStateProperties.FACING).getAxis() == Direction.Axis.Y) {
            // If not pointing top or bottom, and rotation is UP or DOWN, rotate to facing
            blockState = blockState.setValue(BlockStateProperties.FACING, context.getClickedFace());
        } else {
            // Otherwise, just call rotate method
            blockState = blockState.rotate(context.getLevel(), context.getClickedPos(), Rotation.CLOCKWISE_90);
        }
        context.getLevel().setBlockAndUpdate(context.getClickedPos(), blockState);
        return ActionResultType.SUCCESS;
    }

}
