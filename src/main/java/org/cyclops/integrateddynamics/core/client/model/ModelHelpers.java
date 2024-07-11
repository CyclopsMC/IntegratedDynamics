package org.cyclops.integrateddynamics.core.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.Reader;

/**
 * Several helpers for models.
 * @author rubensworks
 */
public final class ModelHelpers {

    public static final BlockModel MODEL_GENERATED = BlockModel.fromString("{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    public static final ItemModelGenerator MODEL_GENERATOR = new ItemModelGenerator();

    /**
     * Read the given model location to a {@link BlockModel}.
     * @param modelLocation A model location (without .json suffix)
     * @return The corresponding model.
     * @throws IOException If the model file was invalid.
     */
    public static BlockModel loadModelBlock(ResourceLocation modelLocation) throws IOException {
        Resource resource = Minecraft.getInstance().getResourceManager().getResource(
                ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), "models/" + modelLocation.getPath() + ".json"))
                .get();
        Reader reader = resource.openAsReader();

        BlockModel model = BlockModel.fromStream(reader);
        model.name = modelLocation.toString();
        return model;
    }

}
