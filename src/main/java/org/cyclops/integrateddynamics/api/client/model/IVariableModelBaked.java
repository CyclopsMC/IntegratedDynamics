package org.cyclops.integrateddynamics.api.client.model;

import net.minecraft.client.renderer.model.IBakedModel;

/**
 * A model for variable items.
 * @author rubensworks
 */
public interface IVariableModelBaked extends IBakedModel {

    /**
     * Set the baked submodels for the given provider.
     * @param provider The provider.
     * @param subModels The baked sub model holder.
     * @param <B> The baked sub model type.
     */
    public <B extends IVariableModelProvider.IBakedModelProvider> void setSubModels(IVariableModelProvider<B> provider, B subModels);

    /**
     * Get the baked submodels for the given provider.
     * @param provider The provider.
     * @param <B> The baked sub model type.
     * @return The baked model provider.
     */
    public <B extends IVariableModelProvider.IBakedModelProvider> B getSubModels(IVariableModelProvider<B> provider);

}
