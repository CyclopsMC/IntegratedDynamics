package org.cyclops.integrateddynamics.capability.cable;

import net.minecraft.util.Direction;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * Default implementation of {@link ICable}.
 * @author rubensworks
 */
public class CableTileMultipartTicking extends CableTile<TileMultipartTicking> {

    public CableTileMultipartTicking(TileMultipartTicking tile) {
        super(tile);
    }

    @Override
    protected boolean isForceDisconnectable() {
        return true;
    }

    @Override
    protected EnumFacingMap<Boolean> getForceDisconnected() {
        return tile.getForceDisconnected();
    }

    @Override
    protected EnumFacingMap<Boolean> getConnected() {
        return tile.getConnected();
    }

    @Override
    public boolean isForceDisconnected(Direction side) {
        if(!tile.getCableFakeable().isRealCable() || tile.getPartContainer().hasPart(side)) return true;
        return super.isForceDisconnected(side);
    }
}
