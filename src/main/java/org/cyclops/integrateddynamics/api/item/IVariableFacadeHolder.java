package org.cyclops.integrateddynamics.api.item;

/**
 * Capability for items that can hold a {@link IVariableFacade}.
 * @author rubensworks
 */
public interface IVariableFacadeHolder {

    /**
     * @return The held variable facade.
     */
    public IVariableFacade getVariableFacade();

}
