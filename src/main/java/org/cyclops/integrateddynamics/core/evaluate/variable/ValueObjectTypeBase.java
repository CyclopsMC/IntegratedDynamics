package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import javax.annotation.Nullable;

/**
 * Base implementation of a value object type.
 * @author rubensworks
 */
public abstract class ValueObjectTypeBase<V extends IValue> extends ValueTypeBase<V> {

    public ValueObjectTypeBase(String typeName, @Nullable Class<V> valueClass) {
        this(typeName, Helpers.RGBToInt(243, 243, 243), TextFormatting.GRAY.toString(), valueClass);
    }

    @Deprecated // TODO: remove, and also remove Nullable option in 1.15
    public ValueObjectTypeBase(String typeName, int color, String colorFormat) {
        super(typeName, color, colorFormat);
    }

    public ValueObjectTypeBase(String typeName, int color, String colorFormat, @Nullable Class<V> valueClass) {
        super(typeName, color, colorFormat, valueClass);
    }

    @Override
    public boolean isObject() {
        return true;
    }
}
