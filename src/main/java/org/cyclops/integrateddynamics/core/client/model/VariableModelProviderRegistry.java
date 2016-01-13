package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProviderRegistry;

import java.util.Collections;
import java.util.List;

/**
 * Registry for {@link IVariableModelProvider}.
 * @author rubensworks
 */
public class VariableModelProviderRegistry implements IVariableModelProviderRegistry {

    private static final VariableModelProviderRegistry INSTANCE = new VariableModelProviderRegistry();

    private final List<IVariableModelProvider<?>> providers = Lists.newLinkedList();

    /**
     * @return The unique instance.
     */
    public static VariableModelProviderRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <E extends IVariableModelProvider<B>, B extends IVariableModelProvider.IBakedModelProvider> E addProvider(E provider) {
        providers.add(provider);
        return provider;
    }

    @Override
    public List<IVariableModelProvider<? extends IVariableModelProvider.IBakedModelProvider>> getProviders() {
        return Collections.unmodifiableList(providers);
    }
}
