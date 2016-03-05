package org.cyclops.integrateddynamics.part.aspect.write;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for itemstack write aspects.
 * @author rubensworks
 */
public abstract class AspectWriteListBase extends AspectWriteBase<ValueTypeList.ValueList, ValueTypeList> {

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
