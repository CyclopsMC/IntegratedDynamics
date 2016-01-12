package org.cyclops.integrateddynamics.client.model;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;

/**
 * A dynamic model for cables.
 * @author rubensworks
 */
public class CableModel extends CableModelBase {

    public CableModel(IExtendedBlockState state, boolean isItemStack) {
        super(state, isItemStack);
    }

    public CableModel() {
        super();
    }

    @Override
    protected boolean isRealCable() {
        return BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.REALCABLE, true);
    }

    @Override
    protected Optional<IBlockState> getFacade() {
        return BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.FACADE, Optional.absent());
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
        return BlockHelpers.getSafeBlockStateProperty(getState(),
                BlockCable.PART_RENDERPOSITIONS[side.ordinal()], IPartType.RenderPosition.NONE);
    }

    @Override
    protected boolean shouldRenderParts() {
        return !GeneralConfig.TESRPartRendering;
    }

    @Override
    protected IBakedModel getPartModel(EnumFacing side) {
        IPartContainer partContainer = BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.PARTCONTAINER, null);
        IBlockState blockState = partContainer != null ? partContainer.getPart(side).getBlockState(partContainer, side) : null;
        Minecraft mc = Minecraft.getMinecraft();
        BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
        BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
        return blockModelShapes.getModelForState(blockState);
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        return new CableModel((IExtendedBlockState) state, false);
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack) {
        return new CableModel((IExtendedBlockState) BlockCable.getInstance().getDefaultState(), true);
    }
}
