package org.cyclops.integrateddynamics.capability.cable;

import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;

/**
 * Implementation of {@link ICableFakeable} for {@link BlockEntityMultipartTicking}.
 * @author rubensworks
 */
public class CableFakeableMultipartTicking extends CableFakeableDefault {

    private final BlockEntityMultipartTicking tile;

    public CableFakeableMultipartTicking(BlockEntityMultipartTicking tile) {
        this.tile = tile;
    }

    @Override
    protected void sendUpdate() {
        tile.sendUpdate();
    }
}
