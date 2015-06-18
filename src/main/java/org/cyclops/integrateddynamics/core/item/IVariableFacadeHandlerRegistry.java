package org.cyclops.integrateddynamics.core.item;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.core.network.IVariableFacade;

/**
 * Registry for retrieving variable facade handlers.
 * @author rubensworks
 */
public interface IVariableFacadeHandlerRegistry extends IRegistry {

    /**
     * Register a new handler.
     * @param variableFacadeHandler The handler.
     */
    public void registerHandler(IVariableFacadeHandler variableFacadeHandler);

    /**
     * Loop through all handlers and uses the first handler that can take the given item.
     * @param itemStack The item.
     * @return The variable facade handled by the first possible handler.
     */
    public IVariableFacade handle(ItemStack itemStack);

}
