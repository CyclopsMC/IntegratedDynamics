package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import net.minecraft.block.AbstractBlock.Properties;

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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIGHT);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockRenderType getRenderShape(BlockState blockState) {
        return BlockRenderType.INVISIBLE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.NORMAL;
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockItemUseContext blockItemUseContext) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return VoxelShapes.empty();
    }

    @Override
    public void fillItemCategory(ItemGroup p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
        // Do not appear in creative tab
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return world.getBlockState(pos).getValue(LIGHT);
    }
}
