package org.cyclops.integrateddynamics.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

/**
 * Indicates items that can be used as wrench.
 * @author rubensworks
 */
public interface IWrench {

    /**
     * Check if this wrench can be used.
     * @param player The player.
     * @param pos The position that is wrenched.
     * @return If it can be used.
     */
    public boolean canUse(EntityPlayer player, BlockPos pos);

    /**
     * Called before the wrench is being used after the canUse check if done.
     * @param player The player.
     * @param pos The position that is wrenched.
     */
    public void beforeUse(EntityPlayer player, BlockPos pos);

    /**
     * Called after the wrench is used.
     * @param player The player.
     * @param pos The position that is wrenched.
     */
    public void afterUse(EntityPlayer player, BlockPos pos);

}
