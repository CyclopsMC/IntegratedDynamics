package org.cyclops.integrateddynamics.capability;

import com.google.common.collect.Maps;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

import java.util.Map;

/**
 * Default implementation of {@link IVariableContainer}.
 * @author rubensworks
 */
public class VariableContainerDefault implements IVariableContainer {

    private final Map<Integer, IVariableFacade> variableCache = Maps.newHashMap();

    @Override
    public Map<Integer, IVariableFacade> getVariableCache() {
        return this.variableCache;
    }
}
