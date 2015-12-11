package org.cyclops.integrateddynamics.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.item.ItemBlockMetadata;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.block.BlockCable;

/**
 * @author rubensworks
 */
public class ItemBlockCable extends ItemBlockMetadata {

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
        return block instanceof ICableFakeable && !((ICableFakeable) block).isRealCable(world, pos);
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

    protected boolean attempItemUseTarget(ItemStack stack, World world, BlockPos pos, BlockCable blockCable) {
        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof ICableFakeable) {
            ICableFakeable cable = (ICableFakeable) block;
            if(!cable.isRealCable(world, pos)) {
                cable.setRealCable(world, pos, true);
                playPlaceSound(world, pos);
                --stack.stackSize;
                blockCable.setDisableCollisionBox(false);
                return true;
            }
        }
        return false;
    }

    public static void playPlaceSound(World world, BlockPos pos) {
        Block block = BlockCable.getInstance();
        world.playSoundEffect((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F),
                block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F);
    }

    public static void playBreakSound(World world, BlockPos pos, IBlockState blockState) {
        world.playAuxSFX(2001, pos, Block.getStateId(blockState));
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
