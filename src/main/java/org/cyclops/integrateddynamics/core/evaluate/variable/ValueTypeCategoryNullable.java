package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.ChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

/**
 * Value type category with values that can be null.
 * @author rubensworks
 */
public class ValueTypeCategoryNullable extends ValueTypeCategoryBase<IValue> {

    public ValueTypeCategoryNullable() {
        super("nullable", Helpers.RGBToInt(100, 100, 100), ChatFormatting.DARK_GRAY, IValue.class);
    }

    public boolean isNull(IVariable a) throws EvaluationException {
        IValueTypeNullable<IValue> type = ValueHelpers.variableUnpackAnyType(a, Operators.NULLABLE_ISNULL, this, IValueTypeNullable.class);
        return type.isNull(a.getValue());
    }

    @Override
    public boolean correspondsTo(IValueType<?> valueType) {
        return super.correspondsTo(valueType) && valueType instanceof IValueTypeNullable;
    }
}
