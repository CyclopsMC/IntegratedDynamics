package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.Helpers;

/**
 * Wildcard value type
 * @author rubensworks
 */
public class ValueTypeCategoryAny extends ValueTypeCategoryBase<IValue> {

    public ValueTypeCategoryAny() {
        super("any", Helpers.RGBToInt(240, 240, 240), "");
    }

}
