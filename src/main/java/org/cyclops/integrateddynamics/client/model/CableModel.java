package org.cyclops.integrateddynamics.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A dynamic facadeModel for cables.
 * @author rubensworks
 */
public class CableModel extends CableModelBase {

    public CableModel(BlockAndTintGetter level, BlockState state, Direction facing, RandomSource rand, ModelData modelData, ChunkSectionLayer renderType) {
        super(level, state, facing, rand, modelData, renderType);
    }

    public CableModel(ItemStack itemStack, ClientLevel world, ItemOwner entity) {
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
    protected BlockStateModel getPartModel(ModelData modelData, Direction side) {
        IPartContainer partContainer = ModelHelpers.getSafeProperty(modelData, BlockCable.PARTCONTAINER, null);
        BlockState blockState = partContainer != null && partContainer.hasPart(side) ? partContainer.getPart(side).getBlockState(partContainer, side) : null;
        Minecraft mc = Minecraft.getInstance();
        BlockStateModelSet blockModelSet = mc.getModelManager().getBlockStateModelSet();
        return blockState != null ? blockModelSet.get(blockState) : blockModelSet.missingModel();
    }

    @Override
    protected IRenderState getRenderState(ModelData modelData) {
        return ModelHelpers.getSafeProperty(modelData, BlockCable.RENDERSTATE, null);
    }

    @Override
    public List<BakedQuad> handleBlockState(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side, RandomSource rand, ModelData extraData, ChunkSectionLayer renderType) {
        return new CableModel(level, state, side, rand, extraData, renderType).getGeneralQuads();
    }

    @Override
    public List<BakedQuad> handleItemState(@Nullable ItemStack stack, @Nullable Level world, @Nullable ItemOwner entity) {
        return new CableModel(stack, (ClientLevel) world, entity).getGeneralQuads();
    }

    @Override
    public UnbakedModel wrapped() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable ResolvedModel parent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String debugName() {
        return Reference.MOD_ID + ":CableModel";
    }
}
