package org.cyclops.integrateddynamics.capability.path;

import net.minecraft.tileentity.TileEntity;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.IPathElement;

/**
 * Implementation of {@link IPathElement} for a tile entity.
 * @author rubensworks
 */
public class PathElementTile<T extends TileEntity> extends PathElementCable {

    private final T tile;
    private final ICable cable;

    public PathElementTile(T tile, ICable cable) {
        this.tile = tile;
        this.cable = cable;
    }

    protected T getTile() {
        return tile;
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
