package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ITransformation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

/**
 * Several helpers for models.
 * @author rubensworks
 */
public final class ModelHelpers {

    public static final ModelBlock MODEL_GENERATED = ModelBlock.deserialize("{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    public static final ItemModelGenerator MODEL_GENERATOR = new ItemModelGenerator();
    public static final FaceBakery FACE_BAKERY = new FaceBakery();
    public static final List<List<BakedQuad>> EMPTY_FACE_QUADS;
    static {
        EMPTY_FACE_QUADS = Lists.newArrayList();
        for(int i = 0; i < 6; i++) {
            EMPTY_FACE_QUADS.add(Collections.<BakedQuad>emptyList());
        }
    }

    /**
     * Read the given model location to a {@link net.minecraft.client.renderer.block.model.ModelBlock}.
     * @param modelLocation A model location (without .json suffix)
     * @return The corresponding model.
     * @throws IOException If the model file was invalid.
     */
    public static ModelBlock loadModelBlock(ResourceLocation modelLocation) throws IOException {
        IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(
                new ResourceLocation(modelLocation.getResourceDomain(), modelLocation.getResourcePath() + ".json"));
        Reader reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
        return ModelBlock.deserialize(reader);
    }

    /**
     * Bake a model.
     * @param model The model to bake.
     * @param bakedTextureGetter The function for retrieving icons from resource locations.
     * @return The baked model.
     */
    public static IFlexibleBakedModel bakeModel(ModelBlock model,
                                                Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        TextureAtlasSprite sprite = bakedTextureGetter.apply(new ResourceLocation(model.resolveTextureName("layer0")));
        return bakeModel(model, sprite, ModelRotation.X0_Y0);
    }

    /**
     * Bake a model.
     * @param model The model to bake.
     * @param icon The icon to use as default texture.
     * @param transformation The rotation of the model.
     * @return The baked model.
     */
    @SuppressWarnings("unchecked")
    public static IFlexibleBakedModel bakeModel(ModelBlock model, TextureAtlasSprite icon,
                                                ITransformation transformation) {
        ModelBlock itemModel = MODEL_GENERATOR.makeItemModel(Minecraft.getMinecraft().getTextureMapBlocks(), model);
        SimpleBakedModel.Builder builder = (new SimpleBakedModel.Builder(itemModel));
        itemModel.textures.put("layer0", icon.getIconName());
        builder.setTexture(icon);

        for(BlockPart blockPart : itemModel.getElements()) {
            for(EnumFacing side : blockPart.mapFaces.keySet()) {
                BlockPartFace blockPartFace = blockPart.mapFaces.get(side);
                builder.addGeneralQuad(makeBakedQuad(blockPart, blockPartFace, icon, side, transformation, false));
            }
        }

        return new IFlexibleBakedModel.Wrapper(builder.makeBakedModel(), Attributes.DEFAULT_BAKED_FORMAT);
    }

    protected static BakedQuad makeBakedQuad(BlockPart blockPart, BlockPartFace blockPartFace,
                                          TextureAtlasSprite icon, EnumFacing side,
                                          ITransformation transformation,
                                          boolean shade) {
        return FACE_BAKERY.makeBakedQuad(blockPart.positionFrom, blockPart.positionTo, blockPartFace, icon, side,
                transformation, blockPart.partRotation, shade, blockPart.shade);
    }

}
