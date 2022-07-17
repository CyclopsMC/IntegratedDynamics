package org.cyclops.integrateddynamics.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.BlockCable;

import java.util.Optional;

/**
 * A dynamic model for cables.
 * @author rubensworks
 */
public class CableModel extends CableModelBase {

    public CableModel(BlockState state, Direction facing, RandomSource rand, ModelData modelData, RenderType renderType) {
        super(state, facing, rand, modelData, renderType);
    }

    public CableModel(ItemStack itemStack, Level world, LivingEntity entity) {
        super(itemStack, world, entity);
    }

    public CableModel() {
        super();
    }

    @Override
    protected boolean isRealCable(ModelData modelData) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.REALCABLE, true);
    }

    @Override
    protected Optional<BlockState> getFacade(ModelData modelData) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.FACADE, Optional.empty());
    }

    @Override
    protected boolean isConnected(ModelData modelData, Direction side) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.CONNECTED[side.ordinal()], false);
    }

    @Override
    protected boolean hasPart(ModelData modelData, Direction side) {
        return getPartRenderPosition(modelData, side) != PartRenderPosition.NONE;
    }

    @Override
    protected PartRenderPosition getPartRenderPosition(ModelData modelData, Direction side) {
        return ModelHelpers.getSafeProperty(modelData,
                BlockCable.PART_RENDERPOSITIONS[side.ordinal()], PartRenderPosition.NONE);
    }

    @Override
    protected boolean shouldRenderParts(ModelData modelData) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.PARTCONTAINER, null) != null;
    }

    @Override
    protected BakedModel getPartModel(ModelData modelData, Direction side) {
        IPartContainer partContainer = ModelHelpers.getSafeProperty(modelData, BlockCable.PARTCONTAINER, null);
        BlockState blockState = partContainer != null && partContainer.hasPart(side) ? partContainer.getPart(side).getBlockState(partContainer, side) : null;
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher blockRendererDispatcher = mc.getBlockRenderer();
        BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
        return blockModelShapes.getBlockModel(blockState);
    }

    @Override
    protected IRenderState getRenderState(ModelData modelData) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.RENDERSTATE, null);
    }

    @Override
    public BakedModel handleBlockState(BlockState state, Direction side, RandomSource rand, ModelData modelData, RenderType renderType) {
        return new CableModel(state, side, rand, modelData, renderType);
    }

    @Override
    public BakedModel handleItemState(ItemStack stack, Level world, LivingEntity entity) {
        return new CableModel(stack, world, entity);
    }
}
