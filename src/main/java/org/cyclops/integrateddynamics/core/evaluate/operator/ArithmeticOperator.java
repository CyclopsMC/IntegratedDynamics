package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for integer operators.
 * @author rubensworks
 */
public class ArithmeticOperator extends BaseOperator {

    public ArithmeticOperator(String operatorName, IFunction function) {
        this(operatorName, 2, function);
    }

    public ArithmeticOperator(String operatorName, int inputLength, IFunction function) {
        super(operatorName, constructInputVariables(inputLength, ValueTypes.INTEGER), ValueTypes.INTEGER, function);
    }

}
