package org.cyclops.integrateddynamics.api.network;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.CapabilityRegistry;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author rubensworks
 */
public class NetworkCapability<T> extends BaseCapability<T, Void> {

    public static <T> NetworkCapability<T> create(ResourceLocation name, Class<T> typeClass) {
        return (NetworkCapability<T>) registry.create(name, typeClass, void.class);
    }

    public static synchronized List<NetworkCapability<?>> getAll() {
        return registry.getAll();
    }

    // INTERNAL

    // Requires explicitly-typed constructor due to ECJ inference failure.
    private static final CapabilityRegistry<NetworkCapability<?>> registry = new CapabilityRegistry<NetworkCapability<?>>((name, type, clazz) -> new NetworkCapability<>(name, type));

    private NetworkCapability(ResourceLocation name, Class<T> typeClass) {
        super(name, typeClass, void.class);
    }

    @ApiStatus.Internal
    @Nullable
    public T getCapability(Map<NetworkCapability<?>, List<ICapabilityProvider<INetwork, Void, ?>>> providers, INetwork network) {
        List<ICapabilityProvider<INetwork, Void, ?>> list = providers.get(this);
        if (list != null) {
            for (var provider : (List<ICapabilityProvider<INetwork, Void, T>>) (List) list) {
                var ret = provider.getCapability(network, null);
                if (ret != null)
                    return ret;
            }
        }
        return null;
    }

}
