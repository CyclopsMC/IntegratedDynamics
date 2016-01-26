package org.cyclops.integrateddynamics.core.helper;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.util.List;

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

    /**
     * Add the given element to a copy of the given list/
     * @param list The list.
     * @param newElement The element.
     * @param <T> The type.
     * @return The new joined list.
     */
    public static <T> List<T> joinList(List<T> list, T newElement) {
        List<T> newList = Lists.newArrayListWithExpectedSize(list.size() + 1);
        newList.addAll(list);
        if(newElement != null) {
            newList.add(newElement);
        }
        return newList;
    }

}
