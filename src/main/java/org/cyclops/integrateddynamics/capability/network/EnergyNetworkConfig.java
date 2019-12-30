package org.cyclops.integrateddynamics.capability.network;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.core.network.EnergyNetwork;

/**
 * Config for the energy network capability.
 * @author rubensworks
 *
 */
public class EnergyNetworkConfig extends CapabilityConfig<IEnergyNetwork> {

    @CapabilityInject(IEnergyNetwork.class)
    public static Capability<IEnergyNetwork> CAPABILITY = null;

    public EnergyNetworkConfig() {
        super(
                CommonCapabilities._instance,
                "energy_network",
                IEnergyNetwork.class,
                new DefaultCapabilityStorage<IEnergyNetwork>(),
                () -> new EnergyNetwork(IngredientComponent.ENERGY)
        );
    }

}
