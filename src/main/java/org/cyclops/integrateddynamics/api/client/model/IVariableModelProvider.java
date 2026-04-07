package org.cyclops.integrateddynamics.api.client.model;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import org.joml.Matrix4fc;

/**
 * A provider of variable overlay models.
 * @param <B> The type of baked facadeModel provider.
 * @author rubensworks
 */
public interface IVariableModelProvider<B extends IVariableModelProvider.BakedModelProvider> {

    /**
     * Load the models for this provider.
     *
     * @param bakingContext The facadeModel baker.
     * @param matrix
     * @return The baked moderl provider.
     */
    public B bakeOverlayModels(ItemModel.BakingContext bakingContext, Matrix4fc matrix);

    /**
     * Load all required models for this facadeModel provider into the given facadeModel loader.
     *
     * @param resolver The list of sub models that can be appended to, which will be registered afterwards.
     */
    void resolveDependencies(ResolvableModel.Resolver resolver);

    /**
     * Provider for baked models.
     */
    public static interface BakedModelProvider {

    }

}
