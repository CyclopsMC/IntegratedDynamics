package org.cyclops.integrateddynamics.api.evaluate;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.List;

/**
 * A capability that can expose values.
 * @author rubensworks
 */
public interface IValueInterface {
    /**
     * Get a list of values.
     *
     * @return A list of values.
     * @throws EvaluationException If an error occurs while constructing or evaluating the values.
     */
    public List<IValue> getValues() throws EvaluationException;
}
