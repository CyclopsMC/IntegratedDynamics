package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;

/**
 * Base class for object operators.
 * @author rubensworks
 */
public abstract class ObjectOperatorBase extends OperatorBase {

    protected ObjectOperatorBase(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                           IFunction function, IConfigRenderPattern renderPattern) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
    }

    @Override
    public String getUnlocalizedType() {
        return "object." + getUnlocalizedObjectType();
    }

    public abstract String getUnlocalizedObjectType();

}
