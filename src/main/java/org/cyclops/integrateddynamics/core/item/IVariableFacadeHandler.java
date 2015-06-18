package org.cyclops.integrateddynamics.core.item;

import net.minecraft.item.ItemStack;
import org.cyclops.integrateddynamics.core.network.IVariableFacade;

/**
 * Handler for retrieving variable facades from items.
 * @author rubensworks
 */
public interface IVariableFacadeHandler {

    /**
     * Check if this handler can handle the given item.
     * @param itemStack The item containing variable facade info.
     * @return If this handler can take this item.
     */
    public boolean canHandle(ItemStack itemStack);

    /**
     * Get the variable facade for the given item.
     * This will only be called after the check {@link IVariableFacadeHandler#canHandle(ItemStack)}.
     * @param itemStack The item containing variable facade info.
     * @return The variable facade
     */
    public IVariableFacade getVariableFacade(ItemStack itemStack);

}
