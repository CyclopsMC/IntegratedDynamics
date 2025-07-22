package org.cyclops.integrateddynamics.api.client.model;

/**
 * A facadeModel for variable items.
 * @author rubensworks
 */
public interface IVariableModelBaked {

    /**
     * Set the baked submodels for the given provider.
     * @param provider The provider.
     * @param subModels The baked sub facadeModel holder.
     * @param <B> The baked sub facadeModel type.
     */
    public <B extends IVariableModelProvider.BakedModelProvider> void setSubModels(IVariableModelProvider<B> provider, B subModels);

    /**
     * Get the baked submodels for the given provider.
     * @param provider The provider.
     * @param <B> The baked sub facadeModel type.
     * @return The baked facadeModel provider.
     */
    public <B extends IVariableModelProvider.BakedModelProvider> B getSubModels(IVariableModelProvider<B> provider);

}
