package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.ChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

/**
 * Wildcard value type
 * @author rubensworks
 */
public class ValueTypeCategoryAny extends ValueTypeCategoryBase<IValue> {

    public ValueTypeCategoryAny() {
        super("any", Helpers.RGBToInt(240, 240, 240), ChatFormatting.RESET, IValue.class);
    }

}
