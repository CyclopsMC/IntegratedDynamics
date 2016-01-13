package org.cyclops.integrateddynamics.core.tileentity;

import com.google.common.collect.Maps;
import lombok.experimental.Delegate;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableNetwork;

import java.util.Map;

/**
 * A tile entity whose block can connect with cables.
 * @author rubensworks
 */
public class TileCableConnectable extends CyclopsTileEntity implements ITileCableNetwork, CyclopsTileEntity.ITickingTile, TileCableNetworkComponent.IConnectionsMapProvider {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);
    @Delegate(types = {ITileCableNetwork.class})
    protected final TileCableNetworkComponent tileCableNetworkComponent = new TileCableNetworkComponent(this);

    @NBTPersist
    private Map<Integer, Boolean> connected = Maps.newHashMap();

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        tileCableNetworkComponent.updateTileEntity();
    }

    @Override
    public Map<Integer, Boolean> getConnections() {
        return connected;
    }
}
