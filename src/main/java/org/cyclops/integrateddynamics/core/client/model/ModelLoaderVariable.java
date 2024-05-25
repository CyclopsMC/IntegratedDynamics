package org.cyclops.integrateddynamics.core.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import java.util.List;

/**
 * Custom model loader for the variable item.
 * @author rubensworks
 */
public class ModelLoaderVariable implements IGeometryLoader<VariableModel> {

    private final List<ResourceLocation> subModels;

    public ModelLoaderVariable(List<ResourceLocation> subModels) {
        this.subModels = subModels;
    }

    @Override
    public VariableModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
        modelContents.remove("loader");
        BlockModel modelBlock = deserializationContext.deserialize(modelContents, BlockModel.class);
        VariableModel variableModel = new VariableModel(modelBlock);
        variableModel.loadSubModels(this.subModels);
        return variableModel;
    }

}
