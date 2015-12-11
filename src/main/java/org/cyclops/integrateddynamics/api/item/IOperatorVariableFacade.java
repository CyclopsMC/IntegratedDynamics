package org.cyclops.integrateddynamics.api.item;

import org.cyclops.integrateddynamics.api.evaluate.expression.IExpression;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;

/**
 * Variable facade for variables determined for operators based on other variables in the network determined by their id.
 * @author rubensworks
 */
public interface IOperatorVariableFacade extends IVariableFacade {

    /**
     * @return The operator this facade represents.
     */
    public IOperator getOperator();

    /**
     * @return The variable ids that define the inputs of this operator instance.
     */
    public int[] getVariableIds();

    /**
     * @return The expression that this operator applies.
     */
    public IExpression getExpression();

}
