package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Custom model loader for the variable item.
 * @author rubensworks
 */
public class VariableLoader implements ICustomModelLoader {

    private static final ResourceLocation LOCATION = new ModelResourceLocation(new ResourceLocation(Reference.MOD_ID, "variable"), "inventory");
    private static final ResourceLocation LOCATION_RAW = new ModelResourceLocation(new ResourceLocation(Reference.MOD_ID, "item/variable_raw"), "inventory");

    public VariableLoader() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // This is an ugly hack...
        // Forge does not call ICustomModelLoader for Items, so we force it by injecting the model manually into the ModelLoader after it has been instantiated.
        // We have to retrieve the model loader from ModelLoader$VanillaLoader, as that is the only place where an instance is stored.

        // The following code emulates: ModelLoader.VanillaLoader.INSTANCE.getLoader().putModel(LOCATION, loadModel(LOCATION));
        Class<?> clazz = null;
        try {
            clazz = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaLoader");
            Field fieldInstance = clazz.getField("INSTANCE");
            fieldInstance.setAccessible(true);
            Object vanillaLoader = fieldInstance.get(null);
            Method getLoader = clazz.getDeclaredMethod("getLoader");
            getLoader.setAccessible(true);
            ModelLoader modelLoader = (ModelLoader) getLoader.invoke(vanillaLoader);
            modelLoader.putModel(LOCATION, loadModel(modelLoader, LOCATION_RAW));
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.equals(LOCATION);
    }

    public IUnbakedModel loadModel(ModelLoader modelLoader, ResourceLocation modelLocation) {
        IUnbakedModel model = loadModel(modelLocation);
        if (model instanceof VariableModel) {
            ((VariableModel) model).loadSubModels(modelLoader);
        }
        return model;
    }

    @Override
    public IUnbakedModel loadModel(ResourceLocation modelLocation) {
        try {
            BlockModel modelBlock = ModelHelpers.loadModelBlock(modelLocation);
            VariableModel model = new VariableModel(modelBlock);
            return model;
        } catch (IOException e) {
            IntegratedDynamics.clog(Level.ERROR, String.format("The model %s could not be loaded.", modelLocation));
            e.printStackTrace();
        }
        return ModelLoaderRegistry.getMissingModel();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

}
