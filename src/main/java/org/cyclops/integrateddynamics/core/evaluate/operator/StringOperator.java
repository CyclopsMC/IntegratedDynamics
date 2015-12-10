package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for string operators.
 * @author rubensworks
 */
public class StringOperator extends OperatorBase {

    public StringOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 1, function, IConfigRenderPattern.INFIX);
    }

    public StringOperator(String symbol, String operatorName, IFunction function, IConfigRenderPattern renderPattern) {
        this(symbol, operatorName, 1, function, renderPattern);
    }

    public StringOperator(String symbol, String operatorName, int inputLength, IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, constructInputVariables(inputLength, ValueTypes.STRING), ValueTypes.STRING, function, renderPattern);
    }

    public StringOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                           IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
    }

    @Override
    public String getUnlocalizedType() {
        return "string";
    }

}
