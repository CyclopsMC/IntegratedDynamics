package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for logical operators.
 * @author rubensworks
 */
public class LogicalOperator extends BaseOperator {

    public LogicalOperator(String operatorName, IFunction function) {
        this(operatorName, 2, function);
    }

    public LogicalOperator(String operatorName, int inputLength, IFunction function) {
        super(operatorName, constructInputVariables(inputLength, ValueTypes.BOOLEAN), ValueTypes.BOOLEAN, function);
    }

}
