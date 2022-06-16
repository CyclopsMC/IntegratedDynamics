package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * A general constant operator.
 * @author josephcsible
 */
public class GeneralConstantOperator extends GeneralOperator {

    public GeneralConstantOperator(String symbol, String operatorName) {
        super(symbol, operatorName, new IValueType[]{ValueTypes.CATEGORY_ANY, ValueTypes.CATEGORY_ANY}, ValueTypes.CATEGORY_ANY,
                variables -> variables.getValue(0), IConfigRenderPattern.PREFIX_2);
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
        for(int i = 0; i < requiredInputLength; i++) {
            IValueType inputType = input[i];
            if(inputType == null) {
                return Component.translatable(L10NValues.OPERATOR_ERROR_NULLTYPE, this.getOperatorName(), Integer.toString(i));
            }
        }
        return null;
    }

    @Override
    public IValueType getConditionalOutputType(IVariable[] input) {
        return input[0].getType();
    }

}
