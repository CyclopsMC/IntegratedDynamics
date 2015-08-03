package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;

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
     */
    public V getValue() throws EvaluationException;

}
