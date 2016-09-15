package org.cyclops.integrateddynamics.block.collidable;

import net.minecraft.block.state.IBlockState;
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
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.block.ICollidable;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Collidable component for parts.
 * @author rubensworks
 */
public class CollidableComponentParts implements ICollidable.IComponent<EnumFacing, BlockCable> {

    protected AxisAlignedBB getPartBoundingBox(World world, BlockPos pos, EnumFacing side) {
        IPartContainer partContainer = PartHelpers.getPartContainer(world, pos);
        return partContainer != null ? partContainer.getPart(side).getPartRenderPosition().getBoundingBox(side) : BlockCable.NULL_AABB;
    }

    @Override
    public Collection<EnumFacing> getPossiblePositions() {
        return Arrays.asList(EnumFacing.VALUES);
    }

    @Override
    public int getBoundsCount(EnumFacing position) {
        return 1;
    }

    @Override
    public boolean isActive(BlockCable block, World world, BlockPos pos, EnumFacing position) {
        IPartContainer partContainer = PartHelpers.getPartContainer(world, pos);
        return partContainer.hasPart(position);
    }

    @Override
    public List<AxisAlignedBB> getBounds(BlockCable block, World world, BlockPos pos, EnumFacing position) {
        return Collections.singletonList(getPartBoundingBox(world, pos, position));
    }

    @Override
    public ItemStack getPickBlock(World world, BlockPos pos, EnumFacing position) {
        IPartContainer partContainer = PartHelpers.getPartContainer(world, pos);
        return partContainer.getPart(position).getPickBlock(world, pos, partContainer.getPartState(position));
    }

    @Override
    public boolean destroy(World world, BlockPos pos, EnumFacing position, EntityPlayer player) {
        if(!world.isRemote) {
            return PartHelpers.removePart(world, pos, position, player, true);
        }
        return false;
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel getBreakingBaseModel(World world, BlockPos pos, EnumFacing position) {
        IPartContainer partContainer = PartHelpers.getPartContainer(world, pos);
        IBlockState cableState = partContainer != null ? partContainer.getPart(position).getBlockState(partContainer, position) : null;
        return RenderHelpers.getBakedModel(cableState);
    }

}
