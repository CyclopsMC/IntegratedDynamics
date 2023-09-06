package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.ChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

/**
 * Value type category with values that have a name.
 * @author rubensworks
 */
public class ValueTypeCategoryNamed extends ValueTypeCategoryBase<IValue> {

    public ValueTypeCategoryNamed() {
        super("named", Helpers.RGBToInt(250, 10, 13), ChatFormatting.RED, IValue.class);
    }

    public String getName(IVariable a) throws EvaluationException {
        IValueTypeNamed<IValue> type = ValueHelpers.variableUnpackAnyType(a, Operators.NAMED_NAME, this, IValueTypeNamed.class);
        return type.getName(a.getValue());
    }

    @Override
    public boolean correspondsTo(IValueType<?> valueType) {
        return super.correspondsTo(valueType) && valueType instanceof IValueTypeNamed;
    }
}
