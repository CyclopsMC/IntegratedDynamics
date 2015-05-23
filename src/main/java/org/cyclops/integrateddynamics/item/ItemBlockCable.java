package org.cyclops.integrateddynamics.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.item.ItemBlockExtended;
import org.cyclops.integrateddynamics.block.BlockCable;

/**
 * @author rubensworks
 */
public class ItemBlockCable extends ItemBlockExtended {

    /**
     * Make a new instance.
     *
     * @param block The blockState instance.
     */
    public ItemBlockCable(Block block) {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player,
                                       ItemStack stack) {
        // Skips client-side entity collision detection for placing cables.
        return world.getBlockState(pos.offset(side)).getBlock().isReplaceable(world, pos);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side,
                             float hitX, float hitY, float hitZ) {
        // Skips server-side entity collision detection for placing cables.
        // We temporary disable the collision box of the cable so that it can be placed even if an entity is in the way.
        BlockCable blockCable = (BlockCable) block;
        blockCable.setDisableCollisionBox(true);
        boolean ret = super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
        blockCable.setDisableCollisionBox(false);
        return ret;
    }

}
