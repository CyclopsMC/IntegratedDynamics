package org.cyclops.integrateddynamics.core.evaluate.expression;

import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;

/**
 * Generic expression that can evaluate expressions with variables to a value.
 * @author rubensworks
 */
public interface IExpression<V extends IValue> extends IVariable<V> {

    /**
     * @return The current evaluation result of the input variables.
     * @throws EvaluationException When something went wrong while evaluating.
     */
    public IValue evaluate() throws EvaluationException;

    /**
     * @return If this expression last evaluation resulted in an error.
     */
    public boolean hasErrored();

}
