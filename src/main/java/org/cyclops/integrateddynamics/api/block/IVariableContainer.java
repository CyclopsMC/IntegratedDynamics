package org.cyclops.integrateddynamics.api.block;

import org.cyclops.integrateddynamics.api.item.IVariableFacade;

import java.util.Map;

/**
 * Capability that can hold {@link IVariableFacade}s.
 * @author rubensworks
 */
public interface IVariableContainer {

    /**
     * @return The stored variable facades for this tile.
     */
    public Map<Integer, IVariableFacade> getVariableCache();

}
