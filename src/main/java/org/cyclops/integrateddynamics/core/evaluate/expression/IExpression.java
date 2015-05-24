package org.cyclops.integrateddynamics.core.evaluate.expression;

import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;

/**
 * Generic expression that can evaluate expressions with variables to a value.
 * @author rubensworks
 */
public interface IExpression {

    /**
     * @return The current evaluation result of the input variables.
     * @throws EvaluationException When something went wrong while evaluating.
     */
    public IValue evaluate() throws EvaluationException;

}
