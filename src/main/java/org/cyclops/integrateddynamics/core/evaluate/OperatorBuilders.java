package org.cyclops.integrateddynamics.core.evaluate;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.build.OperatorBuilder;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Collection of operator builders.
 * @author rubensworks
 */
public class OperatorBuilders {

    // --------------- Logical builders ---------------
    public static final OperatorBuilder LOGICAL = OperatorBuilder.forType(ValueTypes.BOOLEAN).appendKind("logical");
    public static final OperatorBuilder LOGICAL_1 = LOGICAL.inputTypes(1, ValueTypes.BOOLEAN).renderPattern(IConfigRenderPattern.PREFIX_1);
    public static final OperatorBuilder LOGICAL_2 = LOGICAL.inputTypes(2, ValueTypes.BOOLEAN).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- Arithmetic builders ---------------
    public static final OperatorBuilder ARITHMETIC = OperatorBuilder.forType(ValueTypes.CATEGORY_NUMBER).appendKind("arithmetic").conditionalOutputTypeDeriver(new OperatorBuilder.IConditionalOutputTypeDeriver() {
        @Override
        public IValueType getConditionalOutputType(IVariable[] input) {
            IValueType[] original = ValueHelpers.from(input);
            IValueTypeNumber[] types = new IValueTypeNumber[original.length];
            for(int i = 0; i < original.length; i++) {
                types[i] = (IValueTypeNumber) original[i];
            }
            return ValueTypes.CATEGORY_NUMBER.getLowestType(types);
        }
    });
    public static final OperatorBuilder ARITHMETIC_2 = ARITHMETIC.inputTypes(2, ValueTypes.CATEGORY_NUMBER).renderPattern(IConfigRenderPattern.INFIX);
    public static final OperatorBuilder ARITHMETIC_2_PREFIX = ARITHMETIC.inputTypes(2, ValueTypes.CATEGORY_NUMBER).renderPattern(IConfigRenderPattern.PREFIX_2);

}
