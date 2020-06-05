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
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockState blockState = context.getWorld().getBlockState(context.getPos());
        if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
            return super.onItemUse(context);
        }
        if (context.getFace().getAxis() == Direction.Axis.Y
                && blockState.has(BlockStateProperties.FACING)) {
            // If pointing top or bottom, and we can rotate to UP and DOWN, rotate to that direction or opposite
            blockState = blockState.with(BlockStateProperties.FACING, blockState.get(BlockStateProperties.FACING) == Direction.UP ? Direction.DOWN : Direction.UP);
        } else if (context.getFace().getAxis() != Direction.Axis.Y
                && blockState.has(BlockStateProperties.FACING)
                && blockState.get(BlockStateProperties.FACING).getAxis() == Direction.Axis.Y) {
            // If not pointing top or bottom, and rotation is UP or DOWN, rotate to facing
            blockState = blockState.with(BlockStateProperties.FACING, context.getFace());
        } else {
            // Otherwise, just call rotate method
            blockState = blockState.rotate(context.getWorld(), context.getPos(), Rotation.CLOCKWISE_90);
        }
        context.getWorld().setBlockState(context.getPos(), blockState);
        return ActionResultType.SUCCESS;
    }

}
