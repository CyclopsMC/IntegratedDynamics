package org.cyclops.integrateddynamics.api.evaluate;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.Optional;

/**
 * A capability that can expose values.
 * @author rubensworks
 */
public interface IValueInterface {
    /**
     * Get a value.
     *
     * @return A value.
     * @throws EvaluationException If an error occurs while constructing or evaluating the value.
     */
    public Optional<IValue> getValue() throws EvaluationException;
}
