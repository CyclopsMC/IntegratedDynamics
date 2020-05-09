package org.cyclops.integrateddynamics.api.evaluate.variable;

/**
 * A listener for variable invalidations
 * @author rubensworks
 */
public interface IVariableInvalidateListener {

    /**
     * Called when a variable was invalidated.
     */
    public void invalidate();

}
