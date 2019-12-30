package org.cyclops.integrateddynamics.capability.facadeable;

import net.minecraft.block.BlockState;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link IFacadeable}.
 * @author rubensworks
 */
public class FacadeableTileMultipartTicking implements IFacadeable {

    private final TileMultipartTicking tile;

    public FacadeableTileMultipartTicking(TileMultipartTicking tile) {
        this.tile = tile;
    }

    @Override
    public boolean hasFacade() {
        return tile.getFacadeBlockTag() != null;
    }

    @Override
    public BlockState getFacade() {
        if(!hasFacade()) {
            return null;
        }
        return BlockHelpers.deserializeBlockState(tile.getFacadeBlockTag());
    }

    @Override
    public void setFacade(@Nullable BlockState blockState) {
        if(blockState == null) {
            tile.setFacadeBlockTag(null);
        } else {
            tile.setFacadeBlockTag(BlockHelpers.serializeBlockState(blockState));
        }
        tile.sendUpdate();
    }
}
