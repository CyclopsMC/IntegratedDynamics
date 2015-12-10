package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        builder.addAll(Aspects.REGISTRY.getAspectModels());
        builder.addAll(ValueTypes.REGISTRY.getValueTypeModels());
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
            for(String textureName : (List<String>) ItemModelGenerator.LAYERS) {
                String path = base.resolveTextureName(textureName);
                ResourceLocation resourceLocation = new ResourceLocation(path);
                if(!path.equals(textureName)) {
                    textures.add(resourceLocation);
                }
            }
        }

        // Loop over all textures in this model and add them to the collection.
        for(String textureName : (Collection<String>) base.textures.values()) {
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

        // Add aspects to baked model.
        for(IAspect aspect : Aspects.REGISTRY.getAspects()) {
            try {
                IModel model = ModelLoaderRegistry.getModel(Aspects.REGISTRY.getAspectModel(aspect));
                IBakedModel bakedAspectModel = model.bake(state, format, bakedTextureGetter);
                bakedModel.addAspectModel(aspect, bakedAspectModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Add value types to baked model.
        for(IValueType valueType : ValueTypes.REGISTRY.getValueTypes()) {
            try {
                ModelResourceLocation modelResourceLocation = ValueTypes.REGISTRY.getValueTypeModel(valueType);
                if(modelResourceLocation != null) {
                    IModel model = ModelLoaderRegistry.getModel(modelResourceLocation);
                    IBakedModel bakedValueTypeModel = model.bake(state, format, bakedTextureGetter);
                    bakedModel.addValueTypeModel(valueType, bakedValueTypeModel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bakedModel;
    }

    @Override
    public IModelState getDefaultState() {
        return ModelHelpers.DEFAULT_ITEM_STATE;
    }

}
