package org.cyclops.integrateddynamics.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
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

    public CableModel(BlockState state, Direction facing, RandomSource rand, IModelData modelData) {
        super(state, facing, rand, modelData);
    }

    public CableModel(ItemStack itemStack, Level world, LivingEntity entity) {
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
        return ModelHelpers.getSafeProperty(modelData, BlockCable.PARTCONTAINER, null) != null;
    }

    @Override
    protected BakedModel getPartModel(IModelData modelData, Direction side) {
        IPartContainer partContainer = ModelHelpers.getSafeProperty(modelData, BlockCable.PARTCONTAINER, null);
        BlockState blockState = partContainer != null && partContainer.hasPart(side) ? partContainer.getPart(side).getBlockState(partContainer, side) : null;
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher blockRendererDispatcher = mc.getBlockRenderer();
        BlockModelShaper blockModelShapes = blockRendererDispatcher.getBlockModelShaper();
        return blockModelShapes.getBlockModel(blockState);
    }

    @Override
    protected IRenderState getRenderState(IModelData modelData) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.RENDERSTATE, null);
    }

    @Override
    public BakedModel handleBlockState(BlockState state, Direction side, RandomSource rand, IModelData modelData) {
        return new CableModel(state, side, rand, modelData);
    }

    @Override
    public BakedModel handleItemState(ItemStack stack, Level world, LivingEntity entity) {
        return new CableModel(stack, world, entity);
    }
}
