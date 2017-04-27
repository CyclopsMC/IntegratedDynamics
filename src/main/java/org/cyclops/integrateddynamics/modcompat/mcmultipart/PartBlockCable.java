package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.block.BlockCable;

/**
 * @author rubensworks
 */
public class PartBlockCable implements IMultipart {

    private final BlockCable block;

    public PartBlockCable(BlockCable block) {
        this.block = block;
    }

    @Override
    public BlockCable getBlock() {
        return this.block;
    }

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
        return EnumCenterSlot.CENTER;
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
        return EnumCenterSlot.CENTER;
    }
}
