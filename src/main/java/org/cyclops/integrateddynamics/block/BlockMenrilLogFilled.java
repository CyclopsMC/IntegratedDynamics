package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

/**
 * Menril wood block that is filled.
 * @author rubensworks
 */
public class BlockMenrilLogFilled extends LogBlock {

    public static final EnumProperty<Direction> SIDE = BlockStateProperties.HORIZONTAL_FACING;

    public BlockMenrilLogFilled(MaterialColor materialColor, Properties properties) {
        super(materialColor, properties);

        this.setDefaultState(this.stateContainer.getBaseState()
                .with(AXIS, Direction.Axis.X)
                .with(SIDE, Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AXIS, SIDE);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(SIDE, context.getPlacementHorizontalFacing().getOpposite());
    }

}
