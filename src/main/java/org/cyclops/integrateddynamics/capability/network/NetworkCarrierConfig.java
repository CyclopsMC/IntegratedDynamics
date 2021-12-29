package org.cyclops.integrateddynamics.capability.network;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;

/**
 * Config for the network carrier capability.
 * @author rubensworks
 *
 */
public class NetworkCarrierConfig extends CapabilityConfig<INetworkCarrier> {

    public static Capability<INetworkCarrier> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public NetworkCarrierConfig() {
        super(
                CommonCapabilities._instance,
                "network_carrier",
                INetworkCarrier.class
        );
    }

}
