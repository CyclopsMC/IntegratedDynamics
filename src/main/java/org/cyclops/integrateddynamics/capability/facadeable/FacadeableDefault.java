package org.cyclops.integrateddynamics.capability.facadeable;

import net.minecraft.block.BlockState;
import org.cyclops.integrateddynamics.api.block.IFacadeable;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link IFacadeable}.
 * @author rubensworks
 */
public class FacadeableDefault implements IFacadeable {

    @Override
    public boolean hasFacade() {
        return false;
    }

    @Override
    public BlockState getFacade() {
        return null;
    }

    @Override
    public void setFacade(@Nullable BlockState blockState) {

    }
}
