package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 * Model for a variant of a variable item.
 * @author rubensworks
 */
public class VariableModel implements UnbakedModel, IModelGeometry<VariableModel> {

    private final BlockModel base;

    public VariableModel(BlockModel base) {
        this.base = base;
    }

    public void loadSubModels(ForgeModelBakery modelLoader) {
        for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
            provider.loadModels(modelLoader);
        }
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
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        base.parent = ModelHelpers.MODEL_GENERATED; // To enable texture resolving

        Set<Material> textures = Sets.newHashSet(base.getMaterial("particle"));

        // Loop over all textures for the default layers and add them to the collection if available.
        if(base.getRootModel() == ModelBakery.GENERATION_MARKER) {
            ItemModelGenerator.LAYERS.forEach((p_228814_2_) -> {
                textures.add(base.getMaterial(p_228814_2_));
            });
        }

        // Loop over all textures in this model and add them to the collection.
        for(Either<Material, String> texture : base.textureMap.values()) {
            texture.ifLeft(textures::add);
        }

        return textures;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
                                 ModelState transform, ResourceLocation location) {
        Material textureName = base.getMaterial("layer0");
        BlockModel itemModel = ModelHelpers.MODEL_GENERATOR.generateBlockModel(spriteGetter, base);
        SimpleBakedModel.Builder builder = (new SimpleBakedModel.Builder(itemModel.customData, itemModel.getOverrides(bakery, itemModel, spriteGetter)));
        itemModel.textureMap.put("layer0", Either.left(textureName));
        TextureAtlasSprite textureAtlasSprite = spriteGetter.apply(textureName);
        builder.particle(textureAtlasSprite);
        for (BakedQuad bakedQuad : ItemLayerModel.getQuadsForSprite(0, textureAtlasSprite, transform.getRotation())) {
            builder.addUnculledFace(bakedQuad);
        }
        BakedModel baseModel = builder.build();
        VariableModelBaked bakedModel = new VariableModelBaked(baseModel);

        for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
            bakedModel.setSubModels(provider, provider.bakeOverlayModels(bakery, spriteGetter, transform, location));
        }

        return bakedModel;
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
       return bake(bakery, spriteGetter, modelTransform, modelLocation);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return getMaterials(modelGetter, missingTextureErrors);
    }
}
