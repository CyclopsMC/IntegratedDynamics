package org.cyclops.integrateddynamics.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.BlockCable;

import java.util.Optional;
import java.util.Random;

/**
 * A dynamic model for cables.
 * @author rubensworks
 */
public class CableModel extends CableModelBase {

    public CableModel(BlockState state, Direction facing, Random rand, IModelData modelData) {
        super(state, facing, rand, modelData);
    }

    public CableModel(ItemStack itemStack, World world, LivingEntity entity) {
        super(itemStack, world, entity);
    }

    public CableModel() {
        super();
    }

    @Override
    protected boolean isRealCable(IModelData modelData) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.REALCABLE, true);
    }

    @Override
    protected Optional<BlockState> getFacade(IModelData modelData) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.FACADE, Optional.empty());
    }

    @Override
    protected boolean isConnected(IModelData modelData, Direction side) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.CONNECTED[side.ordinal()], false);
    }

    @Override
    protected boolean hasPart(IModelData modelData, Direction side) {
        return getPartRenderPosition(modelData, side) != PartRenderPosition.NONE;
    }

    @Override
    protected PartRenderPosition getPartRenderPosition(IModelData modelData, Direction side) {
        return ModelHelpers.getSafeProperty(modelData,
                BlockCable.PART_RENDERPOSITIONS[side.ordinal()], PartRenderPosition.NONE);
    }

    @Override
    protected boolean shouldRenderParts(IModelData modelData) {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT
                && ModelHelpers.getSafeProperty(modelData, BlockCable.PARTCONTAINER, null) != null;
    }

    @Override
    protected IBakedModel getPartModel(IModelData modelData, Direction side) {
        IPartContainer partContainer = ModelHelpers.getSafeProperty(modelData, BlockCable.PARTCONTAINER, null);
        BlockState blockState = partContainer != null && partContainer.hasPart(side) ? partContainer.getPart(side).getBlockState(partContainer, side) : null;
        Minecraft mc = Minecraft.getInstance();
        BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
        BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
        return blockModelShapes.getModel(blockState);
    }

    @Override
    protected IRenderState getRenderState(IModelData modelData) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.RENDERSTATE, null);
    }

    @Override
    public IBakedModel handleBlockState(BlockState state, Direction side, Random rand, IModelData modelData) {
        return new CableModel(state, side, rand, modelData);
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack, World world, LivingEntity entity) {
        return new CableModel(stack, world, entity);
    }
}
