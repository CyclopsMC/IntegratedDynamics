package org.cyclops.integrateddynamics.api.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;

/**
 * Facade through which a value can be retrieved.
 * @author rubensworks
 */
public interface IVariable<V extends IValue> {

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
     * @return If this aspect has a state that can be invalidated.
     */
    public boolean canInvalidate();

    /**
     * Called when this variable should be invalidated.
     * This is only called when required, so there is no guarantee that this is called in a regular pattern.
     */
    public void invalidate();

    /**
     * Add a dependency relation.
     * This makes it so that when a certain variable gets invalidated,
     * all dependent variables also become invalidated.
     * This invalidation should happen recursively.
     * @param dependent A variable that depends on this.
     */
    public void addDependent(IVariable<?> dependent);

}
