package org.cyclops.integrateddynamics.api.block;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nullable;

/**
 * Capability for targets that can hold facades.
 * @author rubensworks
 */
public interface IFacadeable {

    /**
     * @return If this container has a facade.
     */
    public boolean hasFacade();

    /**
     * @return The blockstate of the facade.
     */
    public IBlockState getFacade();

    /**
     * Set the new facade
     * @param blockState The new facade or null.
     */
    public void setFacade(@Nullable IBlockState blockState);

}
