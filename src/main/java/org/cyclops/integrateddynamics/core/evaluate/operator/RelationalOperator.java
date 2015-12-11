package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for relational operators.
 * @author rubensworks
 */
public class RelationalOperator extends OperatorBase {

    public RelationalOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 2, function, IConfigRenderPattern.INFIX);
    }

    public RelationalOperator(String symbol, String operatorName, int inputLength, IFunction function, IConfigRenderPattern renderPattern) {
        this(symbol, operatorName, constructInputVariables(inputLength, ValueTypes.INTEGER), ValueTypes.BOOLEAN, function, renderPattern);
    }

    protected RelationalOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                                 IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
    }

    @Override
    public String getUnlocalizedType() {
        return "relational";
    }

}
