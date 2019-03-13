package org.cyclops.integrateddynamics.core.tileentity;

import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.InventoryTileEntity;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableTile;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementTile;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

/**
 * A part entity with inventory whose block can connect with cables.
 * @author rubensworks
 */
public class TileCableConnectableInventory extends InventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();

    @Getter
    private final ICable cable;
    private final INetworkCarrier networkCarrier;

    public TileCableConnectableInventory(int inventorySize, String inventoryName, int stackSize) {
        super(inventorySize, inventoryName, stackSize);
        cable = new CableTile<TileCableConnectableInventory>(this) {

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
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        connected.clear();
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (connected.isEmpty()) {
            cable.updateConnections();
        }
        if (getWorld() != null && !getWorld().isRemote) {
            NetworkHelpers.revalidateNetworkElements(getWorld(), getPos());
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

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (getWorld() != null && !getWorld().isRemote) {
            NetworkHelpers.invalidateNetworkElements(getWorld(), getPos(), this);
        }
    }
}
