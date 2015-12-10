package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * A general identity operator.
 * @author rubensworks
 */
public class GeneralIdentityOperator extends GeneralOperator {

    public GeneralIdentityOperator(String symbol, String operatorName) {
        super(symbol, operatorName, new IValueType[]{ValueTypes.CATEGORY_ANY}, ValueTypes.CATEGORY_ANY, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                return variables[0].getValue();
            }
        }, IConfigRenderPattern.PREFIX_1);
    }

    @Override
    public L10NHelpers.UnlocalizedString validateTypes(IValueType[] input) {
        // Input size checking
        int requiredInputLength = getRequiredInputLength();
        if(input.length != requiredInputLength) {
            return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTH,
                    this.getOperatorName(), input.length, requiredInputLength);
        }
        // Input types checking
        for(int i = 0; i < requiredInputLength; i++) {
            IValueType inputType = input[i];
            if(inputType == null) {
                return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_NULLTYPE, this.getOperatorName(), Integer.toString(i));
            }
        }
        return null;
    }

    @Override
    public IValueType getConditionalOutputType(IVariable[] input) {
        return input[0].getType();
    }

}
