package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemModelGenerator;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Model for a variant of a variable item.
 * @author rubensworks
 */
public class VariableModel implements IUnbakedModel {

    private final BlockModel base;

    public VariableModel(BlockModel base) {
        this.base = base;
    }

    public static void addAdditionalModels(ImmutableSet.Builder<ResourceLocation> builder) {
        for(IVariableModelProvider<?> provider : VariableModelProviders.REGISTRY.getProviders()) {
            builder.addAll(provider.getDependencies());
        }
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        if(base.getParentLocation() == null || base.getParentLocation().getPath().startsWith("builtin/")) {
            return Collections.emptyList();
        }
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
        builder.add(base.getParentLocation());
        addAdditionalModels(builder);
        return builder.build();
    }

    @Override
    public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
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
    @Nullable
    public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter,
                            ISprite sprite, VertexFormat format) {
        TextureAtlasSprite textureAtlasSprite = spriteGetter.apply(new ResourceLocation(base.resolveTextureName("layer0")));
        BlockModel itemModel = ModelHelpers.MODEL_GENERATOR.makeItemModel(Minecraft.getInstance().getTextureMap()::getSprite, base);
        SimpleBakedModel.Builder builder = (new SimpleBakedModel.Builder(itemModel, itemModel.getOverrides(bakery, itemModel, spriteGetter, format)));
        itemModel.textures.put("layer0", textureAtlasSprite.getName().toString());
        builder.setTexture(textureAtlasSprite);
        for (BakedQuad bakedQuad : ItemLayerModel.getQuadsForSprite(0, textureAtlasSprite, format, Optional.empty())) {
            builder.addGeneralQuad(bakedQuad);
        }
        IBakedModel baseModel = builder.build();
        VariableModelBaked bakedModel = new VariableModelBaked(baseModel);

        for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
            bakedModel.setSubModels(provider, provider.bakeOverlayModels(bakery, spriteGetter, sprite, format));
        }

        return bakedModel;
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

}
