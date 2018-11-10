package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;

/**
 * Value type category with values that can be null.
 * @author rubensworks
 */
public class ValueTypeCategoryNullable extends ValueTypeCategoryBase<IValue> {

    public ValueTypeCategoryNullable() {
        super("nullable", Helpers.RGBToInt(100, 100, 100), TextFormatting.DARK_GRAY.toString());
    }

    public boolean isNull(IVariable a) throws EvaluationException {
        try {
            return ((IValueTypeNullable) a.getType()).isNull(a.getValue());
        } catch (ClassCastException e) {
            // This can happen with 'any' types.
            return false;
        }
    }

    @Override
    public boolean correspondsTo(IValueType<?> valueType) {
        return super.correspondsTo(valueType) && valueType instanceof IValueTypeNullable;
    }
}
