package org.cyclops.integrateddynamics.core.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/**
 * @author rubensworks
 */
public record ItemModelVariableOverlay(ItemModelVariableOverlays variableModelBaked) implements ItemModel {

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner entity, int seed) {
        ItemModel model = this.variableModelBaked.getModelForItem(stack, level);
        if (model != null) {
            model.update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);
        }
    }

    public static record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<ItemModelVariableOverlay.Unbaked> MAP_CODEC = MapCodec.unit(new ItemModelVariableOverlay.Unbaked());

        @Override
        public MapCodec<ItemModelVariableOverlay.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(BakingContext bakingContext, Matrix4fc matrix) {
            ItemModelVariableOverlays bakedModel = new ItemModelVariableOverlays();
            Matrix4f matrixLocal = new Matrix4f(matrix);
            float offset = 0;
            for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
                offset += 0.01F;
                bakedModel.setSubModels(provider, provider.bakeOverlayModels(bakingContext, matrixLocal.translate(0F, 0F, offset)));
            }
            return new ItemModelVariableOverlay(bakedModel);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
                provider.resolveDependencies(resolver);
            }
        }
    }
}
