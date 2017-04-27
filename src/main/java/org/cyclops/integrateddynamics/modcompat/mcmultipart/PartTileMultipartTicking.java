package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * @author rubensworks
 */
public class PartTileMultipartTicking implements IMultipartTile {

    private final TileMultipartTicking tile;

    public PartTileMultipartTicking(TileMultipartTicking tile) {
        this.tile = tile;
    }

    @Override
    public TileEntity getTileEntity() {
        return this.tile;
    }
}
