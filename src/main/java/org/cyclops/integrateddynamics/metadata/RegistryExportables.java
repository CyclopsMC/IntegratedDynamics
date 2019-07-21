package org.cyclops.integrateddynamics.metadata;

import org.cyclops.cyclopscore.CyclopsCore;
import org.cyclops.cyclopscore.metadata.IRegistryExportableRegistry;

public class RegistryExportables {

    public static IRegistryExportableRegistry REGISTRY = CyclopsCore._instance.getRegistryManager()
            .getRegistry(IRegistryExportableRegistry.class);

    public static void load() {
        REGISTRY.register(new RegistryExportableSqueezerRecipe());
        REGISTRY.register(new RegistryExportableMechanicalSqueezerRecipe());
        REGISTRY.register(new RegistryExportableDryingBasinRecipe());
        REGISTRY.register(new RegistryExportableMechanicalDryingBasinRecipe());
        REGISTRY.register(new RegistryExportableAspect());
    }

}
