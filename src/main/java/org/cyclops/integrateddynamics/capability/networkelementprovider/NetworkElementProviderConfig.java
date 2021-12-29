package org.cyclops.integrateddynamics.capability.networkelementprovider;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;

/**
 * Config for the network element provider capability.
 * @author rubensworks
 *
 */
public class NetworkElementProviderConfig extends CapabilityConfig<INetworkElementProvider> {

    public static Capability<INetworkElementProvider> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public NetworkElementProviderConfig() {
        super(
                CommonCapabilities._instance,
                "network_element_provider",
                INetworkElementProvider.class
        );
    }

}
