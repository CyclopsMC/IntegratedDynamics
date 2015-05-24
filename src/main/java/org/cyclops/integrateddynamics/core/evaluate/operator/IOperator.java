package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;

/**
 * Relation on value types.
 * @author rubensworks
 */
public interface IOperator {

    /**
     * @return The unique name of this operator that will also be used for display.
     */
    public String getOperatorName();

    /**
     * @return The ordered types of values that are used as input for this operator.
     */
    public IValueType[] getInputTypes();

    /**
     * @return The type of value that is achieved when this operator is executed.
     */
    public IValueType getOutputType();

    /**
     * Evaluate the given input values for this operator.
     * @param input The ordered input values.
     * @return The output value.
     * @throws EvaluationException When something went wrong while evaluating.
     */
    public IValue evaluate(IVariable[] input) throws EvaluationException;

}
