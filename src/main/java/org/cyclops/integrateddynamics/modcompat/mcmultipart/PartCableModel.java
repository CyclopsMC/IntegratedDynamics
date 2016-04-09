package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.client.model.CableModelBase;

/**
 * A dynamic model for cables.
 * @author rubensworks
 */
public class PartCableModel extends CableModelBase {

    public PartCableModel(IBlockState blockState, EnumFacing facing, long rand) {
        super(blockState, facing, rand);
    }

    public PartCableModel(ItemStack itemStack, World world, EntityLivingBase entity) {
        super(itemStack, world, entity);
    }

    public PartCableModel() {
        super();
    }

    @Override
    protected boolean isRealCable() {
        return true;
    }

    @Override
    protected Optional<IBlockState> getFacade() {
        return Optional.absent();
    }

    @Override
    protected boolean isConnected(EnumFacing side) {
        return BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.CONNECTED[side.ordinal()], false);
    }

    @Override
    protected boolean hasPart(EnumFacing side) {
        return getPartRenderPosition(side) != IPartType.RenderPosition.NONE;
    }

    @Override
    protected IPartType.RenderPosition getPartRenderPosition(EnumFacing side) {
        return BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.PART_RENDERPOSITIONS[side.ordinal()], IPartType.RenderPosition.NONE);
    }

    @Override
    protected boolean shouldRenderParts() {
        return false;
    }

    @Override
    protected IBakedModel getPartModel(EnumFacing side) {
        return null;
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state, EnumFacing side, long rand) {
        return new PartCableModel(state, side, rand);
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack, World world, EntityLivingBase entity) {
        return new PartCableModel(stack, world, entity);
    }
}
