package org.cyclops.integrateddynamics.api.client.model;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;

/**
 * A provider of variable overlay models.
 * @param <B> The type of baked model provider.
 * @author rubensworks
 */
public interface IVariableModelProvider<B extends IVariableModelProvider.BakedModelProvider> {

    /**
     * Load the models for this provider.
     *
     * @param modelBaker The model baker.
     * @param transform  The model transformation.
     * @return The baked moderl provider.
     */
    public B bakeOverlayModels(ModelBaker modelBaker, ModelState transform);

    /**
     * @return All models this provider depends on and should thus be loaded.
     */
    public Collection<ResourceLocation> getDependencies();

    /**
     * Load all required models for this model provider into the given model loader.
     * @param subModels The list of sub models that can be appended to, which will be registered afterwards.
     */
    void loadModels(List<ResourceLocation> subModels);

    /**
     * Provider for baked models.
     */
    public static interface BakedModelProvider {

    }

}
