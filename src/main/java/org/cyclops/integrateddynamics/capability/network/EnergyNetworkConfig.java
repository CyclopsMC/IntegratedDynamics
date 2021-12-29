package org.cyclops.integrateddynamics.capability.network;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;

/**
 * Config for the energy network capability.
 * @author rubensworks
 *
 */
public class EnergyNetworkConfig extends CapabilityConfig<IEnergyNetwork> {

    public static Capability<IEnergyNetwork> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public EnergyNetworkConfig() {
        super(
                CommonCapabilities._instance,
                "energy_network",
                IEnergyNetwork.class
        );
    }

}
