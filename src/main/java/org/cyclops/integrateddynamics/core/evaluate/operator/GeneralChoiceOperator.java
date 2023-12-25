package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * A general choice operator.
 * @author rubensworks
 */
public class GeneralChoiceOperator extends GeneralOperator {

    public GeneralChoiceOperator(String symbol, String operatorName, String interactName) {
        super(symbol, operatorName, interactName, new IValueType[]{ValueTypes.BOOLEAN, ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY}, ValueTypes.CATEGORY_ANY, new IFunction() {
            @Override
            public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
                boolean a = variables.getValue(0, ValueTypes.BOOLEAN).getRawValue();
                return a ? variables.getValue(1) : variables.getValue(2);
            }
        }, new IConfigRenderPattern.Base(100, 22, new Pair[]{Pair.of(6, 2), Pair.of(60, 2) , Pair.of(80, 2)}, Pair.of(40, 2)));
    }

    @Override
    public MutableComponent validateTypes(IValueType[] input) {
        // Input size checking
        int requiredInputLength = getRequiredInputLength();
        if(input.length != requiredInputLength) {
            return Component.translatable(L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTH,
                    this.getOperatorName(), input.length, requiredInputLength);
        }
        // Input types checking
        IValueType temporarySecondInputType = null;
        for(int i = 0; i < requiredInputLength; i++) {
            IValueType inputType = input[i];
            if(inputType == null) {
                return Component.translatable(L10NValues.OPERATOR_ERROR_NULLTYPE, this.getOperatorName(), Integer.toString(i));
            }
            if(i == 0 && !ValueHelpers.correspondsTo(getInputTypes()[i], inputType)) {
                return Component.translatable(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        this.getOperatorName(), Component.translatable(inputType.getTranslationKey()),
                        Integer.toString(i), Component.translatable(getInputTypes()[i].getTranslationKey()));
            } else if(i == 1) {
                temporarySecondInputType = inputType;
            } else if(i == 2) {
                if(!ValueHelpers.correspondsTo(temporarySecondInputType, inputType)) {
                    return Component.translatable(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                            this.getOperatorName(), Component.translatable(inputType.getTranslationKey()),
                            Integer.toString(i), Component.translatable(temporarySecondInputType.getTranslationKey()));
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
