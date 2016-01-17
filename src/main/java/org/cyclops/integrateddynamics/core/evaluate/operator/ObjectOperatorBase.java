package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;

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
        return getUnlocalizedObjectType();
    }

    public abstract String getUnlocalizedObjectType();

}
