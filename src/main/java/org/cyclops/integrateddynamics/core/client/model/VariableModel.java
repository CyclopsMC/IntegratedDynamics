package org.cyclops.integrateddynamics.core.client.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.List;

/**
 * Model for a variant of a variable item.
 * @author rubensworks
 */
public class VariableModel implements UnbakedModel {

    private final BlockModel base;

    public VariableModel(BlockModel base) {
        this.base = base;
    }

    public void loadSubModels(List<ResourceLocation> subModels) {
        for(IVariableModelProvider<? extends IVariableModelProvider.BakedModelProvider> provider : VariableModelProviders.REGISTRY.getProviders()) {
            provider.loadModels(subModels);
        }
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        this.base.resolveDependencies(resolver);
        for (IVariableModelProvider<? extends IVariableModelProvider.BakedModelProvider> provider : VariableModelProviders.REGISTRY.getProviders()) {
            for (ResourceLocation dependency : provider.getDependencies()) {
                resolver.resolve(dependency);
            }
        }
    }

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        BakedModel baseModel = new ItemModelGenerator().bake(textures, baker, modelState, useAmbientOcclusion, usesBlockLight, itemTransforms, additionalProperties);
        VariableModelBaked bakedModel = new VariableModelBaked(baseModel);

        for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
            bakedModel.setSubModels(provider, provider.bakeOverlayModels(baker, modelState));
        }

        return bakedModel;
    }

    @Override
    public BakedModel bake(TextureSlots textureSlots, ModelBaker baker, ModelState modelState, boolean hasAmbientOcclusion, boolean useBlockLight, ItemTransforms transforms) {
        return this.bake(textureSlots, baker, modelState, hasAmbientOcclusion, useBlockLight, transforms, ContextMap.EMPTY);
    }
}
