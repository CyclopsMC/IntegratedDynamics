package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for block itemstacks read aspects.
 * @author rubensworks
 */
public abstract class AspectReadObjectItemStackBase extends AspectReadObjectBase<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> {

    @Override
    public String getUnlocalizedObjectType() {
        return "itemstack." + getUnlocalizedItemStackType();
    }

    protected abstract String getUnlocalizedItemStackType();

    @Override
    public ValueObjectTypeItemStack getValueType() {
        return ValueTypes.OBJECT_ITEMSTACK;
    }

}
