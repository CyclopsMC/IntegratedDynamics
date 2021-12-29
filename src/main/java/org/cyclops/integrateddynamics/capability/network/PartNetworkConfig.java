package org.cyclops.integrateddynamics.capability.network;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;

/**
 * Config for the part network capability.
 * @author rubensworks
 *
 */
public class PartNetworkConfig extends CapabilityConfig<IPartNetwork> {

    public static Capability<IPartNetwork> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public PartNetworkConfig() {
        super(
                CommonCapabilities._instance,
                "part_network",
                IPartNetwork.class
        );
    }

}
