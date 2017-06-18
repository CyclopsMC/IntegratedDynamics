package org.cyclops.integrateddynamics.capability.network;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.core.network.EnergyNetwork;

/**
 * Config for the energy network capability.
 * @author rubensworks
 *
 */
public class EnergyNetworkConfig extends CapabilityConfig {

    /**
     * The unique instance.
     */
    public static EnergyNetworkConfig _instance;

    @CapabilityInject(IEnergyNetwork.class)
    public static Capability<IEnergyNetwork> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public EnergyNetworkConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "energy_network",
                "A capability for networks that can hold energy.",
                IEnergyNetwork.class,
                new DefaultCapabilityStorage<IEnergyNetwork>(),
                EnergyNetwork.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
