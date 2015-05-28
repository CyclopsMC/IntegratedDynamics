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
import org.cyclops.integrateddynamics.block.ICableConnectable;

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

    protected boolean checkCableAt(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof ICableConnectable && !((ICableConnectable) block).isRealCable(world, pos);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player,
                                       ItemStack stack) {
        BlockPos target = pos.offset(side);
        // First check if the target is an unreal cable.
        if(checkCableAt(world, pos)) return true;
        // Then check if the target is covered by an unreal cable at the given side.
        if(checkCableAt(world, target)) return true;
        // Skips client-side entity collision detection for placing cables.
        return world.getBlockState(target).getBlock().isReplaceable(world, pos);
    }

    protected boolean attempItemUseTarget(ItemStack stack, World worldIn, BlockPos pos, BlockCable blockCable) {
        Block block = worldIn.getBlockState(pos).getBlock();
        if(block instanceof ICableConnectable) {
            ICableConnectable cable = (ICableConnectable) block;
            if(!cable.isRealCable(worldIn, pos)) {
                cable.setRealCable(worldIn, pos, true);
                --stack.stackSize;
                blockCable.setDisableCollisionBox(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side,
                             float hitX, float hitY, float hitZ) {
        // Skips server-side entity collision detection for placing cables.
        // We temporary disable the collision box of the cable so that it can be placed even if an entity is in the way.
        BlockCable blockCable = (BlockCable) block;
        blockCable.setDisableCollisionBox(true);

        // Avoid regular block placement when the target is an unreal cable.
        if(attempItemUseTarget(stack, worldIn, pos, blockCable)) return true;

        // Change pos and side when we are targeting a block that is blocked by an unreal cable, so we want to target
        // the unreal cable.
        if(attempItemUseTarget(stack, worldIn, pos.offset(side), blockCable)) return true;

        boolean ret = super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
        blockCable.setDisableCollisionBox(false);
        return ret;
    }

}
