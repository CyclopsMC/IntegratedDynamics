package org.cyclops.integrateddynamics.client.render.model;

import net.minecraft.client.color.item.Constant;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic facadeModel for facade items.
 * @author rubensworks
 */
public class FacadeModel implements ItemModel {

    private final ItemModel emptyModel;
    private final ModelRenderProperties modelrenderproperties;

    public FacadeModel(ItemModel emptyModel, ModelRenderProperties modelrenderproperties) {
        this.emptyModel = emptyModel;
        this.modelrenderproperties = modelrenderproperties;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @org.jetbrains.annotations.Nullable ClientLevel level, @org.jetbrains.annotations.Nullable ItemOwner entity, int seed) {
        BlockState blockState = RegistryEntries.ITEM_FACADE.get().getFacadeBlock(itemStack);
        if(blockState == null) {
            emptyModel.update(renderState, itemStack, itemModelResolver, displayContext, level, entity, seed);
        } else {
            BlockStateModel bakedModel = IModHelpers.get().getRenderHelpers().getBakedModel(blockState);
            List<BlockStateModelPart> parts = new ArrayList<>();
            bakedModel.collectParts(RandomSource.create(seed), parts);
            QuadCollection.Builder quadBuilder = new QuadCollection.Builder();
            for (BlockStateModelPart collectPart : parts) {
                for (Direction direction : Direction.values()) {
                    for (BakedQuad quad : collectPart.getQuads(direction)) {
                        quadBuilder.addCulledFace(direction, quad);
                    }
                }
            }
            new CuboidItemModelWrapper(List.of(new Constant(-1)), quadBuilder.build(), this.modelrenderproperties, new Matrix4f()).update(renderState, itemStack, itemModelResolver, displayContext, level, entity, seed);
        }
    }
}
