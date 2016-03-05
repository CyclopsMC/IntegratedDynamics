package org.cyclops.integrateddynamics.core.part.aspect.build;

/**
 * Used for forwarding values to a next propagator.
 * @author rubensworks
 */
public interface IAspectValuePropagator<I, O> {

    public O getOutput(I input);

}
