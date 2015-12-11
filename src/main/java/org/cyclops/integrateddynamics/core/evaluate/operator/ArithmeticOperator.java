package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for arithmetic operators.
 * @author rubensworks
 */
public class ArithmeticOperator extends OperatorBase {

    public ArithmeticOperator(String symbol, String operatorName, IFunction function) {
        this(symbol, operatorName, 2, function, IConfigRenderPattern.INFIX);
    }

    public ArithmeticOperator(String symbol, String operatorName, IFunction function, IConfigRenderPattern renderPattern) {
        this(symbol, operatorName, 2, function, renderPattern);
    }

    public ArithmeticOperator(String symbol, String operatorName, int inputLength, IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, constructInputVariables(inputLength, ValueTypes.CATEGORY_NUMBER), ValueTypes.CATEGORY_NUMBER, function, renderPattern);
    }

    @Override
    public String getUnlocalizedType() {
        return "arithmetic";
    }

    @Override
    public IValueType getConditionalOutputType(IVariable[] input) {
        IValueType[] original = ValueHelpers.from(input);
        IValueTypeNumber[] types = new IValueTypeNumber[original.length];
        for(int i = 0; i < original.length; i++) {
            types[i] = (IValueTypeNumber) original[i];
        }
        return ValueTypes.CATEGORY_NUMBER.getLowestType(types);
    }

}
