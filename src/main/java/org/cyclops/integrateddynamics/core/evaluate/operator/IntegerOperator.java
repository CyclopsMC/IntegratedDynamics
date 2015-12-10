package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for integer operators.
 * @author rubensworks
 */
public class IntegerOperator extends OperatorBase {

    public IntegerOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 2, function, IConfigRenderPattern.INFIX);
    }

    public IntegerOperator(String symbol, String operatorName, IFunction function, IConfigRenderPattern renderPattern) {
        this(symbol, operatorName, 2, function, renderPattern);
    }

    public IntegerOperator(String symbol, String operatorName, int inputLength, IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, constructInputVariables(inputLength, ValueTypes.INTEGER), ValueTypes.INTEGER, function, renderPattern);
    }

    @Override
    public String getUnlocalizedType() {
        return "integer";
    }

}
