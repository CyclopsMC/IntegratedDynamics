package org.cyclops.integrateddynamics.api.item;

import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

/**
 * Capability for items that can hold a {@link IVariableFacade}.
 * @author rubensworks
 */
public interface IVariableFacadeHolder {

    /**
     * @return The held variable facade.
     * @param valueDeseralizationContext
     */
    public IVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext);

}
