package org.cyclops.integrateddynamics.core.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.client.model.DynamicItemAndBlockModel;
import org.cyclops.integrateddynamics.client.model.CableModel;
import org.cyclops.integrateddynamics.client.model.CableModelBase;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

/**
 * @author rubensworks
 */
public record ItemModelCable(DynamicItemAndBlockModel model) implements ItemModel {
    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner entity, int seed) {
        ModelRenderProperties modelRenderProperties = model.getModelRenderProperties();
        List<BakedQuad> quadList = this.model.handleItemState(stack, level, entity);
        QuadCollection.Builder quadBuilder = new QuadCollection.Builder();
        for (BakedQuad quad : quadList) {
            quadBuilder.addUnculledFace(quad);
        }
        new CuboidItemModelWrapper(List.of(), quadBuilder.build(), modelRenderProperties, new Matrix4f())
                .update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);
    }

    public static record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<ItemModelCable.Unbaked> MAP_CODEC = MapCodec.unit(new ItemModelCable.Unbaked());

        @Override
        public MapCodec<ItemModelCable.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(BakingContext bakingContext, org.joml.Matrix4fc matrix) {
            CableModelBase.MODEL_BAKER = bakingContext.blockModelBaker(); // Yes, this is a hack...
            return new ItemModelCable(new CableModel());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            // Do nothing
        }
    }
}
