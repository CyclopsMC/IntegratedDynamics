package org.cyclops.integrateddynamics.api.client.model;

import org.cyclops.cyclopscore.init.IRegistry;

import java.util.List;

/**
 * Registry for {@link IVariableModelProvider}.
 * @author rubensworks
 */
public interface IVariableModelProviderRegistry extends IRegistry {

    /**
     * Register a new provider.
     * @param provider The provider to register.
     * @param <E> The type of provider.
     * @param <B> The type of the baked model provider.
     * @return The registered provider
     */
    public <E extends IVariableModelProvider<B>, B extends IVariableModelProvider.IBakedModelProvider> E addProvider(E provider);

    /**
     * @return All registered provider.
     */
    public List<IVariableModelProvider<? extends IVariableModelProvider.IBakedModelProvider>> getProviders();

}
