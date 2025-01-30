package org.cyclops.integrateddynamics.core.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.SimpleModelState;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Unbaked model for variable overlays.
 * @author rubensworks
 */
public class UnbakedModelVariableOverlays implements UnbakedModel {

    public void loadSubModels(List<ResourceLocation> subModels) {
        for(IVariableModelProvider<? extends IVariableModelProvider.BakedModelProvider> provider : VariableModelProviders.REGISTRY.getProviders()) {
            provider.loadModels(subModels);
        }
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        for (IVariableModelProvider<? extends IVariableModelProvider.BakedModelProvider> provider : VariableModelProviders.REGISTRY.getProviders()) {
            for (ResourceLocation dependency : provider.getDependencies()) {
                resolver.resolve(dependency);
            }
        }
    }

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        BakedModelVariableOverlays bakedModel = new BakedModelVariableOverlays();

        // Small offset is required to prevent Z-fighting, similar to DynamicFluidContainerModel
        Transformation overlayTransform = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1.002f), new Quaternionf());
        for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
            ModelState overlayModelStateState = new SimpleModelState(modelState.getRotation().compose(overlayTransform), modelState.isUvLocked());
            bakedModel.setSubModels(provider, provider.bakeOverlayModels(baker, overlayModelStateState));
        }

        return bakedModel;
    }

    @Override
    public BakedModel bake(TextureSlots textureSlots, ModelBaker baker, ModelState modelState, boolean hasAmbientOcclusion, boolean useBlockLight, ItemTransforms transforms) {
        return this.bake(textureSlots, baker, modelState, hasAmbientOcclusion, useBlockLight, transforms, ContextMap.EMPTY);
    }
}
