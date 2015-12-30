package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/**
 * Helper methods.
 * @author rubensworks
 */
public final class Helpers {

    /**
     * Get the fluidstack from the given itemstack.
     * @param itemStack The itemstack.
     * @return The fluidstack or null.
     */
    public static FluidStack getFluidStack(ItemStack itemStack) {
        FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemStack);
        if(fluidStack == null && itemStack.getItem() instanceof IFluidContainerItem) {
            fluidStack = ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack);
        }
        return fluidStack;
    }

    /**
     * Get the fluidstack capacity from the given itemstack.
     * @param itemStack The itemstack.
     * @return The capacity
     */
    public static int getFluidStackCapacity(ItemStack itemStack) {
        if(itemStack.getItem() instanceof IFluidContainerItem) {
            return ((IFluidContainerItem) itemStack.getItem()).getCapacity(itemStack);
        }
        return FluidContainerRegistry.getContainerCapacity(itemStack);
    }

}
