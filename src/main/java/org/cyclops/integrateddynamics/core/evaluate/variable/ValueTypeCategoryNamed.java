package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * Value type category with values that have a name.
 * @author rubensworks
 */
public class ValueTypeCategoryNamed extends ValueTypeCategoryBase<IValue> {

    public ValueTypeCategoryNamed() {
        super("named", Helpers.RGBToInt(250, 10, 13), ChatFormatting.RED, IValue.class);
    }

    public String getName(IVariable a) throws EvaluationException {
        IValueType type = a.getType();
        if (type == ValueTypes.CATEGORY_ANY) {
            // Special case: if the variable has category type ANY, unpack the precise value type to determine the name.
            type = a.getValue().getType();
            if (!(type instanceof IValueTypeNamed)) {
                throw new EvaluationException(new TranslatableComponent(L10NValues.OPERATOR_ERROR_WRONGTYPE,
                        Operators.NAMED_NAME.getLocalizedNameFull(),
                        new TranslatableComponent(type.getTranslationKey()),
                        "0",
                        new TranslatableComponent(this.getTranslationKey())));
            }
        }
        return ((IValueTypeNamed) type).getName(a.getValue());
    }

    @Override
    public boolean correspondsTo(IValueType<?> valueType) {
        return super.correspondsTo(valueType) && valueType instanceof IValueTypeNamed;
    }
}
