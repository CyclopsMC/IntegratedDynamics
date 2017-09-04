package org.cyclops.integrateddynamics.capability.networkelementprovider;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;

/**
 * Config for the network element provider capability.
 * @author rubensworks
 *
 */
public class NetworkElementProviderConfig extends CapabilityConfig<INetworkElementProvider> {

    /**
     * The unique instance.
     */
    public static NetworkElementProviderConfig _instance;

    @CapabilityInject(INetworkElementProvider.class)
    public static Capability<INetworkElementProvider> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public NetworkElementProviderConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "network_element_provider",
                "Providers network elements.",
                INetworkElementProvider.class,
                new DefaultCapabilityStorage<INetworkElementProvider>(),
                NetworkElementProviderEmpty.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
