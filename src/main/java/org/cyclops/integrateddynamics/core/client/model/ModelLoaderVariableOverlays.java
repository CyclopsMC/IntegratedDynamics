package org.cyclops.integrateddynamics.core.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

import java.util.List;

/**
 * Custom model loader for the variable item.
 * @author rubensworks
 */
public class ModelLoaderVariableOverlays implements UnbakedModelLoader<UnbakedModelVariableOverlays> {

    private final List<ResourceLocation> subModels;

    public ModelLoaderVariableOverlays(List<ResourceLocation> subModels) {
        this.subModels = subModels;
    }

    @Override
    public UnbakedModelVariableOverlays read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
        UnbakedModelVariableOverlays variableModel = new UnbakedModelVariableOverlays();
        variableModel.loadSubModels(this.subModels);
        return variableModel;
    }

}
