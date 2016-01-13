package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * Logic programmer element types for the fluidstack value type.
 * @author rubensworks
 */
public class ValueObjectTypeFluidStackElementType extends SingleElementType<ValueTypeItemStackElement> {
    public ValueObjectTypeFluidStackElementType() {
        super(new ILogicProgrammerElementConstructor<ValueTypeItemStackElement>() {
            @Override
            public ValueTypeItemStackElement construct() {
                return new ValueTypeItemStackElement<>(ValueTypes.OBJECT_FLUIDSTACK, new ValueTypeItemStackElement.IItemStackToValue<ValueObjectTypeFluidStack.ValueFluidStack>() {
                    @Override
                    public L10NHelpers.UnlocalizedString validate(ItemStack itemStack) {
                        return Helpers.getFluidStack(itemStack) != null ? null : new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID);
                    }

                    @Override
                    public ValueObjectTypeFluidStack.ValueFluidStack getValue(ItemStack itemStack) {
                        return ValueObjectTypeFluidStack.ValueFluidStack.of(Helpers.getFluidStack(itemStack));
                    }
                }, LogicProgrammerElementTypes.OBJECT_FLUIDSTACK_TYPE);
            }
        }, "fluidstack");
    }
}
