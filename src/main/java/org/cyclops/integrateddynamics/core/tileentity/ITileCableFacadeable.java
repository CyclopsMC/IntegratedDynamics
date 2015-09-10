package org.cyclops.integrateddynamics.core.tileentity;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nullable;

/**
 * Interface for tile entities behind block that are a {@link org.cyclops.integrateddynamics.core.block.cable.ICableFacadeable}.
 * @author rubensworks
 */
public interface ITileCableFacadeable extends ITileCable {

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
