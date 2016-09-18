package org.cyclops.integrateddynamics.core.tileentity;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableTile;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementTile;

/**
 * A part entity whose block can connect with cables.
 * @author rubensworks
 */
public class TileCableConnectable extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    protected final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();

    @Getter
    private final ICable cable;

    public TileCableConnectable() {
        cable = new CableTile<TileCableConnectable>(this) {

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
        addCapabilityInternal(NetworkCarrierConfig.CAPABILITY, new NetworkCarrierDefault());
        addCapabilityInternal(PathElementConfig.CAPABILITY, new PathElementTile(this, cable));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (connected.isEmpty()) {
            cable.updateConnections();
        }
    }
}
