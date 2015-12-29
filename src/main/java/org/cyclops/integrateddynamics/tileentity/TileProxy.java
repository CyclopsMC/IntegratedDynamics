package org.cyclops.integrateddynamics.tileentity;

import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;

/**
 * A tile entity for the variable proxy.
 * @author rubensworks
 */
public class TileProxy extends TileCableConnectableInventory {

    public TileProxy() {
        super(3, "proxy", 64);
    }

}
