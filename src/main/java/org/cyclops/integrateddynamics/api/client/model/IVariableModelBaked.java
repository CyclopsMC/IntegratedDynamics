package org.cyclops.integrateddynamics.api.client.model;

import net.minecraft.client.resources.model.BakedModel;

/**
 * A model for variable items.
 * @author rubensworks
 */
public interface IVariableModelBaked extends BakedModel {

    /**
     * Set the baked submodels for the given provider.
     * @param provider The provider.
     * @param subModels The baked sub model holder.
     * @param <B> The baked sub model type.
     */
    public <B extends IVariableModelProvider.BakedModelProvider> void setSubModels(IVariableModelProvider<B> provider, B subModels);

    /**
     * Get the baked submodels for the given provider.
     * @param provider The provider.
     * @param <B> The baked sub model type.
     * @return The baked model provider.
     */
    public <B extends IVariableModelProvider.BakedModelProvider> B getSubModels(IVariableModelProvider<B> provider);

}
