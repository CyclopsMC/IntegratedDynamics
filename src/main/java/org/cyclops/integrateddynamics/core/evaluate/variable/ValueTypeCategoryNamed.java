package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Sets;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;

/**
 * Value type category with values that have a name.
 * @author rubensworks
 */
public class ValueTypeCategoryNamed extends ValueTypeCategoryBase<IValue> {

    public ValueTypeCategoryNamed() {
        super("named", Helpers.RGBToInt(250, 10, 13), EnumChatFormatting.RED.toString(),
                Sets.<IValueType<?>>newHashSet(ValueTypes.OBJECT_BLOCK));
    }

    public String getName(IVariable a) throws EvaluationException {
        return ((IValueTypeNamed) a.getType()).getName(a.getValue());
    }

}
