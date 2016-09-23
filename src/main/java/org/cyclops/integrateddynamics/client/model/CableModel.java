package org.cyclops.integrateddynamics.client.model;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.BlockCable;

/**
 * A dynamic model for cables.
 * @author rubensworks
 */
public class CableModel extends CableModelBase {

    public CableModel(IExtendedBlockState state, EnumFacing facing, long rand) {
        super(state, facing, rand);
    }

    public CableModel(ItemStack itemStack, World world, EntityLivingBase entity) {
        super(itemStack, world, entity);
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
        return getPartRenderPosition(side) != PartRenderPosition.NONE;
    }

    @Override
    protected PartRenderPosition getPartRenderPosition(EnumFacing side) {
        return BlockHelpers.getSafeBlockStateProperty(getState(),
                BlockCable.PART_RENDERPOSITIONS[side.ordinal()], PartRenderPosition.NONE);
    }

    @Override
    protected boolean shouldRenderParts() {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT
                && BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.PARTCONTAINER, null) != null;
    }

    @Override
    protected IBakedModel getPartModel(EnumFacing side) {
        IPartContainer partContainer = BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.PARTCONTAINER, null);
        IBlockState blockState = partContainer != null && partContainer.hasPart(side) ? partContainer.getPart(side).getBlockState(partContainer, side) : null;
        Minecraft mc = Minecraft.getMinecraft();
        BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
        BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
        return blockModelShapes.getModelForState(blockState);
    }

    @Override
    protected IRenderState getRenderState() {
        return BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.RENDERSTATE, null);
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state, EnumFacing side, long rand) {
        return new CableModel((IExtendedBlockState) state, side, rand);
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack, World world, EntityLivingBase entity) {
        return new CableModel(stack, world, entity);
    }
}
