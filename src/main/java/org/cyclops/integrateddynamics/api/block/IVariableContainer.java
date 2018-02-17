package org.cyclops.integrateddynamics.api.block;

import net.minecraft.inventory.IInventory;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;

import java.util.Map;

/**
 * Capability that can hold {@link IVariableFacade}s.
 * @author rubensworks
 */
public interface IVariableContainer {

    /**
     * @return The stored variable facades for this part.
     */
    public Map<Integer, IVariableFacade> getVariableCache();

    /**
     * Invalidate variables in this cache, clear the cache and re-populate from the supplied inventory
     * @param network {@link INetwork} that the variables are in
     * @param inventory IInventory to re-populate the cache from
     * @param sendVariablesUpdateEvent if true post a VariableContentsUpdatedEvent to the network when done
     */
    public void refreshVariables(INetwork network, IInventory inventory, boolean sendVariablesUpdateEvent);

}
