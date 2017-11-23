package org.cyclops.integrateddynamics.core.tileentity;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TankInventoryTileEntity;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableTile;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementTile;

/**
 * A part entity with tank inventory whose block can connect with cables.
 * @author rubensworks
 */
public class TileCableConnectableTankInventory extends TankInventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();

    @Getter
    private final ICable cable;
    private final INetworkCarrier networkCarrier;

    public TileCableConnectableTankInventory(int inventorySize, String inventoryName, int stackSize, int tankSize, String tankName) {
        super(inventorySize, inventoryName, stackSize, tankSize, tankName);
        cable = new CableTile<TileCableConnectableTankInventory>(this) {

            @Override
            protected boolean isForceDisconnectable() {
                return false;
            }

            @Override
            protected EnumFacingMap<Boolean> getForceDisconnected() {
                return null;
            }

            @Override
            protected EnumFacingMap<Boolean> getConnected() {
                return tile.connected;
            }
        };
        addCapabilityInternal(CableConfig.CAPABILITY, cable);
        networkCarrier = new NetworkCarrierDefault();
        addCapabilityInternal(NetworkCarrierConfig.CAPABILITY, networkCarrier);
        addCapabilityInternal(PathElementConfig.CAPABILITY, new PathElementTile<>(this, cable));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (connected.isEmpty()) {
            cable.updateConnections();
        }
    }

    /**
     * Called after the network has been fully initialized
     */
    public void afterNetworkReAlive() {

    }

    public INetwork getNetwork() {
        return this.networkCarrier.getNetwork();
    }

}
