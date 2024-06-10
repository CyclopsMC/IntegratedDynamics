package org.cyclops.integrateddynamics.api.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Event for when an {@link INetwork} is being constructed.
 * Next to capabilities, also {@link IFullNetworkListener}'s can be added to the network.
 * @author rubensworks
 */
public class AttachCapabilitiesEventNetwork extends Event implements IModBusEvent {

    private final INetwork network;
    private final List<IFullNetworkListener> fullNetworkListeners;
    private final Map<NetworkCapability<?>, List<ICapabilityProvider<INetwork, Void, ?>>> providers;

    public AttachCapabilitiesEventNetwork(INetwork network) {
        this.network = network;
        this.fullNetworkListeners = Lists.newArrayList();
    this.providers = Maps.newIdentityHashMap();
    }

    public INetwork getNetwork() {
        return this.network;
    }

    public void addFullNetworkListener(IFullNetworkListener fullNetworkListener) {
        this.fullNetworkListeners.add(fullNetworkListener);
    }

    public List<IFullNetworkListener> getFullNetworkListeners() {
        return fullNetworkListeners;
    }

    public <T> void register(
            NetworkCapability<T> capability,
            ICapabilityProvider<INetwork, Void, T> provider
    ) {
        Objects.requireNonNull(provider);
        List<ICapabilityProvider<INetwork, Void, ?>> list = providers.computeIfAbsent(capability, k -> Lists.newArrayList());
        list.add(provider);
    }

    public Map<NetworkCapability<?>, List<ICapabilityProvider<INetwork, Void, ?>>> getProviders() {
        return providers;
    }
}
