package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for logical operators.
 * @author rubensworks
 */
public class LogicalOperator extends BaseOperator {

    public LogicalOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 2, function);
    }

    public LogicalOperator(String symbol, String operatorName, int inputLength, IFunction function) {
        super(symbol, operatorName, constructInputVariables(inputLength, ValueTypes.BOOLEAN), ValueTypes.BOOLEAN, function);
    }

    @Override
    public String getUnlocalizedType() {
        return "logical." + getOperatorName();
    }

}
