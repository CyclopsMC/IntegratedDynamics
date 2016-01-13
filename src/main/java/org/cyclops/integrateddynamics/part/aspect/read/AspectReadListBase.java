package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for list read aspects.
 * @author rubensworks
 */
public abstract class AspectReadListBase extends AspectReadBase<ValueTypeList.ValueList, ValueTypeList> {

    @Override
    public String getUnlocalizedType() {
        return super.getUnlocalizedType() + ".list." + getUnlocalizedListType();
    }

    protected abstract String getUnlocalizedListType();

    @Override
    public ValueTypeList getValueType() {
        return ValueTypes.LIST;
    }

}
