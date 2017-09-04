package org.cyclops.integrateddynamics.capability.network;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;

/**
 * Config for the network carrier capability.
 * @author rubensworks
 *
 */
public class NetworkCarrierConfig extends CapabilityConfig<INetworkCarrier> {

    /**
     * The unique instance.
     */
    public static NetworkCarrierConfig _instance;

    @CapabilityInject(INetworkCarrier.class)
    public static Capability<INetworkCarrier> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public NetworkCarrierConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "network_carrier",
                "Capability that can hold networks",
                INetworkCarrier.class,
                new DefaultCapabilityStorage<INetworkCarrier>(),
                NetworkCarrierDefault.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
