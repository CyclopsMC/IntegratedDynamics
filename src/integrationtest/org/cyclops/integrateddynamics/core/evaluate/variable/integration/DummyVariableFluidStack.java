package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Dummy fluidstack variable.
 * @author rubensworks
 */
public class DummyVariableFluidStack extends DummyVariable<ValueObjectTypeFluidStack.ValueFluidStack> {

    public DummyVariableFluidStack(ValueObjectTypeFluidStack.ValueFluidStack value) {
        super(ValueTypes.OBJECT_FLUIDSTACK, value);
    }

    public DummyVariableFluidStack() {
        super(ValueTypes.OBJECT_FLUIDSTACK);
    }

}
