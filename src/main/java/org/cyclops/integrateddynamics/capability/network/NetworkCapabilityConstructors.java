package org.cyclops.integrateddynamics.capability.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.network.AttachCapabilitiesEventNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.core.network.EnergyNetwork;
import org.cyclops.integrateddynamics.core.network.PartNetwork;

/**
 * Constructor event for network capabilities.
 * @author rubensworks
 */
public class NetworkCapabilityConstructors {

    @SubscribeEvent
    public void onNetworkLoad(AttachCapabilitiesEventNetwork event) {
        INetwork network = event.getNetwork();
        PartNetwork partNetwork = new PartNetwork();
        EnergyNetwork energyNetwork = new EnergyNetwork(IngredientComponent.ENERGY);
        energyNetwork.setNetwork(network);
        IEnergyStorage energyChannel = energyNetwork.getChannelExternal(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, IPositionedAddonsNetwork.DEFAULT_CHANNEL);

        event.register(Capabilities.PartNetwork.NETWORK, new DefaultCapabilityProvider<>(partNetwork));
        event.register(Capabilities.EnergyNetwork.NETWORK, new DefaultCapabilityProvider<>(energyNetwork));
        event.register(Capabilities.EnergyStorage.NETWORK, new DefaultCapabilityProvider<>(energyChannel));

        event.addFullNetworkListener(partNetwork);
        event.addFullNetworkListener(energyNetwork);
    }

}
