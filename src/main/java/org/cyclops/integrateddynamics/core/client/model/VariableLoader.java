package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        return modelLocation.getNamespace().equals(Reference.MOD_ID)
               && modelLocation.getPath().equals(LOCATION);
    }

    @Override
    public IUnbakedModel loadModel(ResourceLocation modelLocation) {
        try {
            BlockModel modelBlock = ModelHelpers.loadModelBlock(modelLocation);
            return new VariableModel(modelBlock);
        } catch (IOException e) {
            IntegratedDynamics.clog(Level.ERROR, String.format("The model %s could not be loaded.", modelLocation));
        }
        return ModelLoaderRegistry.getMissingModel();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    public void validateModels() { // TODO: call this somewhere
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
        VariableModel.addAdditionalModels(builder);
        ImmutableSet<ResourceLocation> models = builder.build();
        for(ResourceLocation model : models) {
            if(!ModelLoaderRegistry.loaded(model)) {
                //IntegratedDynamics.clog(Level.ERROR, String.format("Model file %s not found, it is required by the variable item model.", model));
                throw new RuntimeException(String.format("Model file %s not found, it is required by the variable item model.", model));
            }
        }
    }
}
