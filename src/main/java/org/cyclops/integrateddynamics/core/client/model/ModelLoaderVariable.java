package org.cyclops.integrateddynamics.core.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.IModelLoader;

/**
 * Custom model loader for the variable item.
 * @author rubensworks
 */
public class ModelLoaderVariable implements IModelLoader<VariableModel> {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    @Override
    public VariableModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        modelContents.remove("loader");
        BlockModel modelBlock = deserializationContext.deserialize(modelContents, BlockModel.class);
        VariableModel variableModel = new VariableModel(modelBlock);
        variableModel.loadSubModels(ForgeModelBakery.instance());
        return variableModel;
    }

}
