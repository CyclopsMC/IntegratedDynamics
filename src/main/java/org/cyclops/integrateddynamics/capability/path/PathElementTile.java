package org.cyclops.integrateddynamics.capability.path;

import net.minecraft.tileentity.TileEntity;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * Implementation of {@link IPathElement} for {@link TileMultipartTicking}.
 * @author rubensworks
 */
public class PathElementTile extends PathElementCable {

    private final TileEntity tile;
    private final ICable cable;

    public PathElementTile(TileEntity tile, ICable cable) {
        this.tile = tile;
        this.cable = cable;
    }

    @Override
    protected ICable getCable() {
        return cable;
    }

    @Override
    public DimPos getPosition() {
        return DimPos.of(tile.getWorld(), tile.getPos());
    }
}
