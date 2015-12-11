package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * A relational equals operator.
 * @author rubensworks
 */
public class RelationalEqualsOperator extends RelationalOperator {

    public RelationalEqualsOperator(String symbol, String operatorName) {
        super(symbol, operatorName, new IValueType[]{ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY}, ValueTypes.BOOLEAN, new IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                return ValueTypeBoolean.ValueBoolean.of(variables[0].getValue().equals(variables[1].getValue()));
            }
        }, IConfigRenderPattern.INFIX);
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
        IValueType temporarySecondInputType = null;
        for(int i = 0; i < requiredInputLength; i++) {
            IValueType inputType = input[i];
            if(inputType == null) {
                return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_NULLTYPE, this.getOperatorName(), Integer.toString(i));
            }
            if(i == 0) {
                temporarySecondInputType = inputType;
            } else if(i == 1) {
                if(temporarySecondInputType != inputType) {
                    return new L10NHelpers.UnlocalizedString(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                            this.getOperatorName(), new L10NHelpers.UnlocalizedString(inputType.getUnlocalizedName()),
                            Integer.toString(i), new L10NHelpers.UnlocalizedString(temporarySecondInputType.getUnlocalizedName()));
                }
            }
        }
        return null;
    }

}
