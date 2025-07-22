package org.cyclops.integrateddynamics.capability.facadeable;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link IFacadeable}.
 * @author rubensworks
 */
public class FacadeableTileMultipartTicking implements IFacadeable {

    private final BlockEntityMultipartTicking tile;

    public FacadeableTileMultipartTicking(BlockEntityMultipartTicking tile) {
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
        return IModHelpers.get().getBlockHelpers().deserializeBlockState(ValueDeseralizationContext.of(tile.getLevel()).holderLookupProvider().lookupOrThrow(Registries.BLOCK), tile.getFacadeBlockTag());
    }

    @Override
    public void setFacade(@Nullable BlockState blockState) {
        if(blockState == null) {
            tile.setFacadeBlockTag(null);
        } else {
            tile.setFacadeBlockTag(IModHelpers.get().getBlockHelpers().serializeBlockState(blockState));
        }
        tile.setForceLightCheckAtClient(true);
        tile.setChanged();
        tile.sendUpdate();
    }
}
