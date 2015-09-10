package org.cyclops.integrateddynamics.core.block.cable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.core.path.IPathElement;

import javax.annotation.Nullable;

/**
 * Interface for cables that support facades.
 * @author rubensworks
 */
public interface ICableFacadeable<E extends IPathElement<E>> extends ICable<E> {

    /**
     * @param world The world.
     * @param pos The position of this block.
     * @return If this container has a facade.
     */
    public boolean hasFacade(IBlockAccess world, BlockPos pos);

    /**
     * @param world The world.
     * @param pos The position of this block.
     * @return The blockstate of the facade.
     */
    public IBlockState getFacade(World world, BlockPos pos);

    /**
     * Set the new facade
     * @param world The world.
     * @param pos The position of this block.
     * @param blockState The new facade or null.
     */
    public void setFacade(World world, BlockPos pos, @Nullable IBlockState blockState);

}
