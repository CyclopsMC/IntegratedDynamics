package org.cyclops.integrateddynamics.core.client.model;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProviderRegistry;

/**
 * Collection of variable model providers.
 * @author rubensworks
 */
public class VariableModelProviders {

    public static final IVariableModelProviderRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableModelProviderRegistry.class);

    public static final ValueTypeVariableModelProvider VALUETYPE = REGISTRY.addProvider(new ValueTypeVariableModelProvider());
    public static final AspectVariableModelProvider ASPECT = REGISTRY.addProvider(new AspectVariableModelProvider());
    public static final ProxyVariableModelProvider PROXY = REGISTRY.addProvider(new ProxyVariableModelProvider());

    public static void load() {}

}
