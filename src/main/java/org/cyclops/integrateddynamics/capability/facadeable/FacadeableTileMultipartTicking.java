package org.cyclops.integrateddynamics.capability.facadeable;

import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;
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
        return tile.getFacadeBlockName() != null && !tile.getFacadeBlockName().isEmpty();
    }

    @Override
    public IBlockState getFacade() {
        if(!hasFacade()) {
            return null;
        }
        return BlockHelpers.deserializeBlockState(Pair.of(tile.getFacadeBlockName(), tile.getFacadeMeta()));
    }

    @Override
    public void setFacade(@Nullable IBlockState blockState) {
        if(blockState == null) {
            tile.setFacadeMeta(0);
            tile.setFacadeBlockName(null);
        } else {
            Pair<String, Integer> serializedBlockState = BlockHelpers.serializeBlockState(blockState);
            tile.setFacadeMeta(serializedBlockState.getRight());
            tile.setFacadeBlockName(serializedBlockState.getLeft());
        }
        tile.sendUpdate();
    }
}
