package org.cyclops.integrateddynamics.api.network;

import org.cyclops.cyclopscore.datastructure.DimPos;

/**
 * A network element that exists at a certain position.
 * @author rubensworks
 */
public interface IPositionedNetworkElement extends INetworkElement {

    public DimPos getPosition();

}
