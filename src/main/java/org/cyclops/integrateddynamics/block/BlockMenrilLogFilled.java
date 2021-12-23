package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

import net.minecraft.block.AbstractBlock.Properties;

/**
 * Menril wood block that is filled.
 * @author rubensworks
 */
public class BlockMenrilLogFilled extends RotatedPillarBlock {

    public static final EnumProperty<Direction> SIDE = BlockStateProperties.HORIZONTAL_FACING;

    public BlockMenrilLogFilled(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(SIDE, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AXIS, SIDE);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(SIDE, context.getHorizontalDirection().getOpposite());
    }

}
