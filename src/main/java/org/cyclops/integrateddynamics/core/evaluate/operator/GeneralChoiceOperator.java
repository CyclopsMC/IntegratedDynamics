package org.cyclops.integrateddynamics.core.evaluate.operator;

import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;

/**
 * A general choice operator.
 * @author rubensworks
 */
public class GeneralChoiceOperator extends GeneralOperator {

    public GeneralChoiceOperator(String symbol, String operatorName) {
        super(symbol, operatorName, new IValueType[]{ValueTypes.BOOLEAN, ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY}, ValueTypes.CATEGORY_ANY, new OperatorBase.IFunction() {
            @Override
            public IValue evaluate(IVariable... variables) throws EvaluationException {
                boolean a = ((ValueTypeBoolean.ValueBoolean) variables[0].getValue()).getRawValue();
                return a ? variables[1].getValue() : variables[2].getValue();
            }
        }, new IConfigRenderPattern.Base(100, 22, new Pair[]{Pair.of(6, 2), Pair.of(60, 2) , Pair.of(80, 2)}, Pair.of(40, 2)));
    }

    @Override
    public L10NHelpers.UnlocalizedString validateTypes(IValueType[] input) {
        // Input size checking
        int requiredInputLength = getRequiredInputLength();
        if(input.length != requiredInputLength) {
            return new L10NHelpers.UnlocalizedString("operator.error.wrongInputLength",
                    this.getOperatorName(), input.length, requiredInputLength);
        }
        // Input types checking
        IValueType temporarySecondInputType = null;
        for(int i = 0; i < requiredInputLength; i++) {
            IValueType inputType = input[i];
            if(inputType == null) {
                return new L10NHelpers.UnlocalizedString("operator.error.nullType", this.getOperatorName(), Integer.toString(i));
            }
            if(i == 0 && getInputTypes()[i] != inputType) {
                return new L10NHelpers.UnlocalizedString("operator.error.wrongType",
                        this.getOperatorName(), new L10NHelpers.UnlocalizedString(inputType.getUnlocalizedName()),
                        Integer.toString(i), new L10NHelpers.UnlocalizedString(getInputTypes()[i].getUnlocalizedName()));
            } else if(i == 1) {
                temporarySecondInputType = inputType;
            } else if(i == 2) {
                if(temporarySecondInputType != inputType) {
                    return new L10NHelpers.UnlocalizedString("operator.error.wrongType",
                            this.getOperatorName(), new L10NHelpers.UnlocalizedString(inputType.getUnlocalizedName()),
                            Integer.toString(i), new L10NHelpers.UnlocalizedString(temporarySecondInputType.getUnlocalizedName()));
                }
            }
        }
        return null;
    }

    @Override
    public IValueType getConditionalOutputType(IVariable[] input) {
        return input[1].getType();
    }

}
