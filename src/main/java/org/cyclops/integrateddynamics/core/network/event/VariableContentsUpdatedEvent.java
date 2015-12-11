package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.api.network.IPartNetwork;

/**
 * An event used to signal network elements of updated variables inside the network.
 * @author rubensworks
 */
public class VariableContentsUpdatedEvent extends NetworkEvent<IPartNetwork> {

    public VariableContentsUpdatedEvent(IPartNetwork network) {
        super(network);
    }

}
