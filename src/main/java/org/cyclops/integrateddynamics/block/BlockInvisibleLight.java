package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An invisible light source with variable intensity.
 * @author rubensworks
 */
public class BlockInvisibleLight extends Block {

    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    public BlockInvisibleLight(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT);
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.INVISIBLE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.NORMAL;
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockItemUseContext) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return Shapes.empty();
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return world.getBlockState(pos).getValue(LIGHT);
    }
}
