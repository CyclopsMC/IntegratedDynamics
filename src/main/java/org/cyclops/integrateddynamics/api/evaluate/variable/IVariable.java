package org.cyclops.integrateddynamics.api.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;

/**
 * Facade through which a value can be retrieved.
 * @author rubensworks
 */
public interface IVariable<V extends IValue> extends IVariableInvalidateListener {

    /**
     * @return The type of value this variable provides.
     */
    public IValueType<V> getType();

    /**
     * @return The current value of this variable.
     * @throws EvaluationException If something went wrong while evaluating
     */
    public V getValue() throws EvaluationException;

    /**
     * Add a dependency relation.
     *
     * This makes it so that when this variable gets invalidated,
     * the given listener/variables also becomes invalidated.
     *
     * This invalidation should happen recursively for variables.
     *
     * This listener will be removed after the first invalidation.
     * If needed, the listener can be re-attached after that.
     *
     * @param invalidateListener A listener for invalidations.
     */
    public void addInvalidationListener(IVariableInvalidateListener invalidateListener);

}
