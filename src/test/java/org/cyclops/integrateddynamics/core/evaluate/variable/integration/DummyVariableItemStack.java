package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Dummy itemstack variable.
 * @author rubensworks
 */
public class DummyVariableItemStack extends DummyVariable<ValueObjectTypeItemStack.ValueItemStack> {

    public DummyVariableItemStack(ValueObjectTypeItemStack.ValueItemStack value) {
        super(ValueTypes.OBJECT_ITEMSTACK, value);
    }

    public DummyVariableItemStack() {
        super(ValueTypes.OBJECT_ITEMSTACK);
    }

}
