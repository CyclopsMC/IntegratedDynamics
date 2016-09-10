package org.cyclops.integrateddynamics.capability.facadeable;

import net.minecraft.block.state.IBlockState;
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
    public IBlockState getFacade() {
        return null;
    }

    @Override
    public void setFacade(@Nullable IBlockState blockState) {

    }
}
