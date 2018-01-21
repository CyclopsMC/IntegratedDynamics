package org.cyclops.integrateddynamics.block.collidable;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.block.ICollidable;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Collidable component for the center of a cable.
 * @author rubensworks
 */
public class CollidableComponentCableCenter implements ICollidable.IComponent<EnumFacing, BlockCable> {

    @Override
    public Collection<EnumFacing> getPossiblePositions() {
        return Arrays.asList(new EnumFacing[]{null});
    }

    @Override
    public int getBoundsCount(EnumFacing position) {
        return 1;
    }

    @Override
    public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
        return CableHelpers.isNoFakeCable(world, pos, position);
    }

    @Override
    public List<AxisAlignedBB> getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
        return Collections.singletonList(block.getCableBoundingBox(null));
    }

    @Override
    public ItemStack getPickBlock(World world, BlockPos pos, EnumFacing position) {
        return new ItemStack(BlockCable.getInstance());
    }

    @Override
    public boolean destroy(World world, BlockPos pos, EnumFacing position, EntityPlayer player, boolean saveState) {
        if (!world.isRemote) {
            CableHelpers.removeCable(world, pos, player);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel getBreakingBaseModel(World world, BlockPos pos, EnumFacing position) {
        return RenderHelpers.getDynamicBakedModel(world, pos);
    }

}
