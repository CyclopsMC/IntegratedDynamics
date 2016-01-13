package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.base.Optional;
import mcmultipart.client.multipart.ISmartMultipartModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.client.model.CableModelBase;

/**
 * A dynamic model for cables.
 * @author rubensworks
 */
public class PartCableModel extends CableModelBase implements ISmartMultipartModel {

    public PartCableModel(IExtendedBlockState state, boolean isItemStack) {
        super(state, isItemStack);
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
        return BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.PART[side.ordinal()], false);
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
    public IBakedModel handleBlockState(IBlockState state) {
        return new PartCableModel((IExtendedBlockState) state, false);
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack) {
        return new PartCableModel((IExtendedBlockState) BlockCable.getInstance().getDefaultState(), true);
    }

    @Override
    public IBakedModel handlePartState(IBlockState state) {
        return handleBlockState(state);
    }
}
