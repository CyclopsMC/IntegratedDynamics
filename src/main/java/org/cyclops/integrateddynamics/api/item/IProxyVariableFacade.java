package org.cyclops.integrateddynamics.api.item;

/**
 * Variable facade for variables that are proxied.
 * @author rubensworks
 */
public interface IProxyVariableFacade extends IVariableFacade {

    /**
     * @return The proxy id.
     */
    public int getProxyId();

}
