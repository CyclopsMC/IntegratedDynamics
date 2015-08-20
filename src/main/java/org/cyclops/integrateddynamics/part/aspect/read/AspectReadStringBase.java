package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for string read aspects.
 * @author rubensworks
 */
public abstract class AspectReadStringBase extends AspectReadBase<ValueTypeString.ValueString, ValueTypeString> {

    @Override
    public String getUnlocalizedType() {
        return super.getUnlocalizedType() + ".string." + getUnlocalizedStringType();
    }

    protected abstract String getUnlocalizedStringType();

    @Override
    public ValueTypeString getValueType() {
        return ValueTypes.STRING;
    }

}
