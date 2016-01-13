package org.cyclops.integrateddynamics.core.client.model;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.apache.logging.log4j.Level;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

import java.io.IOException;

/**
 * Custom model loader for the variable item.
 * @author rubensworks
 */
public class VariableLoader implements ICustomModelLoader {

    private static final String LOCATION = "models/item/variable";

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourceDomain().equals(Reference.MOD_ID)
               && modelLocation.getResourcePath().equals(LOCATION);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        try {
            ModelBlock modelBlock = ModelHelpers.loadModelBlock(modelLocation);
            return new VariableModel(modelBlock);
        } catch (IOException e) {
            IntegratedDynamics.clog(Level.ERROR, String.format("The model %s could not be loaded.", modelLocation));
        }
        return ModelLoaderRegistry.getMissingModel();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}
