package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
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
                        return FluidContainerRegistry.getFluidForFilledItem(itemStack) != null
                                || (itemStack.getItem() instanceof IFluidContainerItem)
                                ? null : new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID);
                    }

                    @Override
                    public ValueObjectTypeFluidStack.ValueFluidStack getValue(ItemStack itemStack) {
                        FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemStack);
                        if(fluidStack == null && itemStack.getItem() instanceof IFluidContainerItem) {
                            fluidStack = ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack);
                        }
                        return ValueObjectTypeFluidStack.ValueFluidStack.of(fluidStack);
                    }
                }, LogicProgrammerElementTypes.OBJECT_FLUIDSTACK_TYPE);
            }
        }, "fluidstack");
    }
}
