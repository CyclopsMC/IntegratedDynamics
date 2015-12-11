package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for binary operators.
 * @author rubensworks
 */
public class BinaryOperator extends OperatorBase {

    public BinaryOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 2, function, IConfigRenderPattern.INFIX);
    }

    public BinaryOperator(String symbol, String operatorName, int inputLength, IFunction function, IConfigRenderPattern renderPattern) {
        this(symbol, operatorName, constructInputVariables(inputLength, ValueTypes.INTEGER), ValueTypes.INTEGER, function, renderPattern);
    }

    protected BinaryOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                             IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
    }

    @Override
    public String getUnlocalizedType() {
        return "binary";
    }

}
