package org.cyclops.integrateddynamics.core.evaluate;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;

/**
 * Used for forwarding values to a next propagator.
 * @author rubensworks
 */
public interface IOperatorValuePropagator<I, O> {

    public O getOutput(I input) throws EvaluationException;

}
