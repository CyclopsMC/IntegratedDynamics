package org.cyclops.integrateddynamics.core.tileentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;

import java.util.Map;

/**
 * A convenience component for tiles that require implementation of the {@link ITileCableNetwork} interface.
 * Don't forget to also call the {@link TileCableNetworkComponent#updateTileEntity()} method when delegating!
 * @author rubensworks
 */
public class TileCableNetworkComponent implements ITileCableNetwork {

    private final CyclopsTileEntity tile;
    private final IConnectionsMapProvider connectionsMapProvider;

    @Getter
    @Setter
    private IPartNetwork network;

    public <T extends CyclopsTileEntity & IConnectionsMapProvider> TileCableNetworkComponent(T tile) {
        this.tile = tile;
        this.connectionsMapProvider = tile;
    }

    public void updateTileEntity() {
        // If the connection data were reset, update the cable connections
        if (getConnections().isEmpty()) {
            updateConnections();
        }
    }

    protected Map<Integer, Boolean> getConnections() {
        return connectionsMapProvider.getConnections();
    }

    @Override
    public void resetCurrentNetwork() {
        if(network != null) setNetwork(null);
    }

    @Override
    public boolean canConnect(ICable connector, EnumFacing side) {
        return true;
    }

    @Override
    public void updateConnections() {
        World world = tile.getWorld();
        for(EnumFacing side : EnumFacing.VALUES) {
            boolean cableConnected = CableNetworkComponent.canSideConnect(world, tile.getPos(), side, (ICable) tile.getBlock());
            getConnections().put(side.ordinal(), cableConnected);
        }
        tile.markDirty();
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        return getConnections().containsKey(side.ordinal()) && getConnections().get(side.ordinal());
    }

    @Override
    public void disconnect(EnumFacing side) {

    }

    @Override
    public void reconnect(EnumFacing side) {

    }

    public static interface IConnectionsMapProvider {

        public Map<Integer, Boolean> getConnections();

    }

}
