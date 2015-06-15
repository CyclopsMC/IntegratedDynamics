package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for integer operators.
 * @author rubensworks
 */
public class IntegerOperator extends BaseOperator {

    public IntegerOperator(String operatorName, IFunction function) {
        this(operatorName, 2, function);
    }

    public IntegerOperator(String operatorName, int inputLength, IFunction function) {
        super(operatorName, constructInputVariables(inputLength, ValueTypes.INTEGER), ValueTypes.INTEGER, function);
    }

}
