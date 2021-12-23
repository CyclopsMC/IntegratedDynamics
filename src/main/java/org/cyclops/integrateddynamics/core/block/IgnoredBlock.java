package org.cyclops.integrateddynamics.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.NonNullList;

/**
 * A block that is not visible to the player.
 * Just used for providing models, until a better way for doing this comes around.
 * @author rubensworks
 */
public class IgnoredBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public IgnoredBlock() {
        super(Block.Properties.of(Material.GLASS));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> list) {
        // Don't show block in creative tab
    }

}
