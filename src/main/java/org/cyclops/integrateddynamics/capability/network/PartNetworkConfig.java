package org.cyclops.integrateddynamics.capability.network;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.network.PartNetwork;

/**
 * Config for the part network capability.
 * @author rubensworks
 *
 */
public class PartNetworkConfig extends CapabilityConfig<IPartNetwork> {

    /**
     * The unique instance.
     */
    public static PartNetworkConfig _instance;

    @CapabilityInject(IPartNetwork.class)
    public static Capability<IPartNetwork> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public PartNetworkConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "part_network",
                "A capability for adding parts to a network.",
                IPartNetwork.class,
                new DefaultCapabilityStorage<IPartNetwork>(),
                PartNetwork.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
