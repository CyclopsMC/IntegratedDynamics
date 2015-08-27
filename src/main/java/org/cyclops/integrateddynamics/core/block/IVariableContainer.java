package org.cyclops.integrateddynamics.core.block;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.item.IVariableFacade;

import java.util.Map;

/**
 * An interface for containers that can hold {@link IVariableFacade}s.
 * @author rubensworks
 */
public interface IVariableContainer {

    /**
     * @return The position this container is at.
     */
    public DimPos getPosition();

    /**
     * @return The stored variable facades for this tile.
     */
    public Map<Integer, IVariableFacade> getVariableCache();

}
