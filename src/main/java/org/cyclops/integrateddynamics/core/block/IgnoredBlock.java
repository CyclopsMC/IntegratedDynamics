package org.cyclops.integrateddynamics.core.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * A block that is not visible to the player.
 * Just used for providing models, until a better way for doing this comes around.
 * @author rubensworks
 */
public class IgnoredBlock extends Block {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public IgnoredBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

}
