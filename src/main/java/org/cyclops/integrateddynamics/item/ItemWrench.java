package org.cyclops.integrateddynamics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * The default wrench for this mod.
 * @author rubensworks
 */
public class ItemWrench extends Item {

    public ItemWrench(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
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
        return InteractionResult.SUCCESS;
    }

}
