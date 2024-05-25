package org.cyclops.integrateddynamics.api.network;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.CapabilityRegistry;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    final List<ICapabilityProvider<INetwork, Void, T>> providers = Lists.newArrayList();

    @ApiStatus.Internal
    @Nullable
    public T getCapability(INetwork network) {
        for (var provider : providers) {
            var ret = provider.getCapability(network, null);
            if (ret != null)
                return ret;
        }
        return null;
    }

}
