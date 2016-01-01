package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.TRSRTransformation;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * Model for a variant of a variable item.
 * @author rubensworks
 */
public class VariableModel implements IModel {

    private final ModelBlock base;

    public VariableModel(ModelBlock base) {
        this.base = base;
    }

    public static void addAdditionalModels(ImmutableSet.Builder<ResourceLocation> builder) {
        for(IVariableModelProvider<?> provider : VariableModelProviders.REGISTRY.getProviders()) {
            builder.addAll(provider.getDependencies());
        }
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        if(base.getParentLocation() == null || base.getParentLocation().getResourcePath().startsWith("builtin/")) {
            return Collections.emptyList();
        }
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
        builder.add(base.getParentLocation());
        addAdditionalModels(builder);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<ResourceLocation> getTextures() {
        Collection<ResourceLocation> textures = Sets.newHashSet();

        base.parent = ModelHelpers.MODEL_GENERATED; // To enable texture resolving

        // Loop over all textures for the default layers and add them to the collection if available.
        if(base.getRootModel().name.equals("generation marker")) {
            for(String textureName : ItemModelGenerator.LAYERS) {
                String path = base.resolveTextureName(textureName);
                ResourceLocation resourceLocation = new ResourceLocation(path);
                if(!path.equals(textureName)) {
                    textures.add(resourceLocation);
                }
            }
        }

        // Loop over all textures in this model and add them to the collection.
        for(String textureName : base.textures.values()) {
            if(!textureName.startsWith("#")) {
                textures.add(new ResourceLocation(textureName));
            }
        }

        return textures;
    }

    @Override
    public IFlexibleBakedModel bake(IModelState state, VertexFormat format,
                                    Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IFlexibleBakedModel baseModel = ModelHelpers.bakeModel(base, bakedTextureGetter);
        VariableModelBaked bakedModel = new VariableModelBaked(baseModel);

        for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
            bakedModel.setSubModels(provider, provider.bakeOverlayModels(state, format, bakedTextureGetter));
        }

        return bakedModel;
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

}
