package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for integer operators.
 * @author rubensworks
 */
public class ArithmeticOperator extends BaseOperator {

    public ArithmeticOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 2, function);
    }

    public ArithmeticOperator(String symbol, String operatorName, int inputLength, IFunction function) {
        super(symbol, operatorName, constructInputVariables(inputLength, ValueTypes.INTEGER), ValueTypes.INTEGER, function);
    }

    @Override
    public String getUnlocalizedType() {
        return "arithmetic." + getOperatorName();
    }

}
