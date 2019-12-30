package org.cyclops.integrateddynamics.core.tileentity;

import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
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

import javax.annotation.Nullable;

/**
 * A part entity with inventory whose block can connect with cables.
 * @author rubensworks
 */
public class TileCableConnectableInventory extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();

    @Getter
    private final ICable cable;
    private final INetworkCarrier networkCarrier;
    private final SimpleInventory inventory;

    public TileCableConnectableInventory(TileEntityType<?> type, int inventorySize, int stackSize) {
        super(type);
        inventory = createInventory(inventorySize, stackSize);
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
        addCapabilityInternal(CableConfig.CAPABILITY, LazyOptional.of(() -> cable));
        networkCarrier = new NetworkCarrierDefault();
        addCapabilityInternal(NetworkCarrierConfig.CAPABILITY, LazyOptional.of(() -> networkCarrier));
        addCapabilityInternal(PathElementConfig.CAPABILITY, LazyOptional.of(() -> new PathElementTile<>(this, cable)));
    }

    protected SimpleInventory createInventory(int inventorySize, int stackSize) {
        return new SimpleInventory(inventorySize, stackSize);
    }


    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        connected.clear();
        inventory.readFromNBT(tag, "inventory");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        inventory.writeToNBT(tag, "inventory");
        return super.write(tag);
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

    @Nullable
    public INetwork getNetwork() {
        return this.networkCarrier.getNetwork();
    }

    public SimpleInventory getInventory() {
        return inventory;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (getWorld() != null && !getWorld().isRemote) {
            NetworkHelpers.invalidateNetworkElements(getWorld(), getPos(), this);
        }
    }

}
