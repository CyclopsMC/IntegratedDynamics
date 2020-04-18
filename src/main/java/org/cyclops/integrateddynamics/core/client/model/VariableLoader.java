package org.cyclops.integrateddynamics.core.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;

/**
 * Custom model loader for the variable item.
 * @author rubensworks
 */
public class VariableLoader implements IModelLoader<VariableModel> {

    public VariableLoader() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        registerModelLoaderReloadListener();
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // This is an ugly hack...
        // Forge does not call ICustomModelLoader for Items, so we force it by injecting the model manually into the ModelLoader after it has been instantiated.
        // We have to retrieve the model loader from ModelLoader$VanillaLoader, as that is the only place where an instance is stored.

        // The following code emulates: ModelLoader.VanillaLoader.INSTANCE.getLoader().putModel(LOCATION, loadModel(LOCATION));
        /*Class<?> clazz = null;
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
        }*/
        // TODO: rm?
    }

    private void registerModelLoaderReloadListener() {
        // This is another ugly hack.
        // The first ugly hack does not handle reload events.
        // Since the reload event will call ModelLoaderRegistry.cache.clear() before actual model reloading,
        // we wrap over that cache object, and hook into the clear method,
        // so that we can inject our custom models before the loading starts.
        // Note that we can not make use of this hack during mod loading because VariableLoader
        // has not always being constructed by the initial ModelLoaderRegistry.cache.clear() call.
        /*Class<?> clazz = null;
        try {
            clazz = Class.forName("net.minecraftforge.client.model.ModelLoaderRegistry");
            Field fieldCache = clazz.getDeclaredField("cache");
            fieldCache.setAccessible(true);

            // Remove final modified
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(fieldCache, fieldCache.getModifiers() & ~Modifier.FINAL);

            Map<ResourceLocation, IUnbakedModel> cacheOld = (Map<ResourceLocation, IUnbakedModel>) fieldCache.get(null);
            fieldCache.set(null, new MapWrapper<ResourceLocation, IUnbakedModel>(cacheOld) {
                @Override
                public void clear() {
                    super.clear();
                    onClientSetup(null);
                }
            });

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }*/
        // TODO: rm?
    }

    @Override
    public IResourceType getResourceType() {
        return VanillaResourceType.MODELS;
    }

    // TODO: rm?
    /*public IUnbakedModel loadModel(ModelLoader modelLoader, ResourceLocation modelLocation) {
        IUnbakedModel model = loadModel(modelLocation);
        if (model instanceof VariableModel) {
            ((VariableModel) model).loadSubModels(modelLoader);
        }
        return model;
    }*/

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public VariableModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        modelContents.remove("loader");
        BlockModel modelBlock = deserializationContext.deserialize(modelContents, BlockModel.class);
        return new VariableModel(modelBlock);
    }

}
