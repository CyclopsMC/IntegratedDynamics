package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.collect.Maps;
import lombok.experimental.Delegate;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.InventoryTileEntity;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;

import java.util.Map;

/**
 * A tile entity with inventory whose block can connect with cables.
 * @author rubensworks
 */
public class TileCableConnectableInventory extends InventoryTileEntity implements ITileCableNetwork, CyclopsTileEntity.ITickingTile, TileCableNetworkComponent.IConnectionsMapProvider {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);
    @Delegate(types = {ITileCableNetwork.class})
    protected final TileCableNetworkComponent tileCableNetworkComponent = new TileCableNetworkComponent(this);

    @NBTPersist
    private Map<Integer, Boolean> connected = Maps.newHashMap();

    public TileCableConnectableInventory(int inventorySize, String inventoryName, int stackSize) {
        super(inventorySize, inventoryName, stackSize);
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        tileCableNetworkComponent.updateTileEntity();
    }

    @Override
    public Map<Integer, Boolean> getConnections() {
        return connected;
    }

    /**
     * Called after the network has been fully initialized
     */
    public void afterNetworkReAlive() {

    }

}
