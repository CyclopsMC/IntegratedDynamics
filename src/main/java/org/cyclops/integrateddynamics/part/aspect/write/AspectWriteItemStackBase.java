package org.cyclops.integrateddynamics.part.aspect.write;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for itemstack write aspects.
 * @author rubensworks
 */
public abstract class AspectWriteItemStackBase extends AspectWriteBase<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> {

    @Override
    public String getUnlocalizedType() {
        return super.getUnlocalizedType() + ".itemstack." + getUnlocalizedItemStackType();
    }

    protected abstract String getUnlocalizedItemStackType();

    @Override
    public ValueObjectTypeItemStack getValueType() {
        return ValueTypes.OBJECT_ITEMSTACK;
    }

}
