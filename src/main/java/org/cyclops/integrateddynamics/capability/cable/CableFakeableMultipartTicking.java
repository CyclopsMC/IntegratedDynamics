package org.cyclops.integrateddynamics.capability.cable;

import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * Implementation of {@link ICableFakeable} for {@link TileMultipartTicking}.
 * @author rubensworks
 */
public class CableFakeableMultipartTicking extends CableFakeableDefault {

    private final TileMultipartTicking tile;

    public CableFakeableMultipartTicking(TileMultipartTicking tile) {
        this.tile = tile;
    }

    @Override
    protected void sendUpdate() {
        tile.sendUpdate();
    }
}
