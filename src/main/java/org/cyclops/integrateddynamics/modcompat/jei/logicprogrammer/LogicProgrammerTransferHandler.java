package org.cyclops.integrateddynamics.modcompat.jei.logicprogrammer;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeSlottedValueChangedPacket;

import javax.annotation.Nullable;

/**
 * Allows recipe transferring to Logic Programmer elements with slots.
 * @author rubensworks
 */
public class LogicProgrammerTransferHandler<T extends ContainerLogicProgrammerBase> implements IRecipeTransferHandler<T> {

    private final Class<T> clazz;

    public LogicProgrammerTransferHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<T> getContainerClass() {
        return clazz;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(T container, IRecipeLayout recipeLayout,
                                               EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        ILogicProgrammerElement element = container.getActiveElement();

        // Always work with ItemStacks
        ItemStack itemStack = null;
        Object focusElement = recipeLayout.getFocus().getValue();
        if (focusElement instanceof ItemStack) {
            itemStack = (ItemStack) focusElement;
        } else if (focusElement instanceof FluidStack) {
            itemStack = new ItemStack(Items.BUCKET);
            IFluidHandler fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            fluidHandler.fill((FluidStack) focusElement, true);
        }

        if (element != null && itemStack != null) {
            if (element.isItemValidForSlot(0, itemStack)) {
                if (doTransfer) {
                    int slotId = container.inventorySlots.size() - 1;
                    container.putStackInSlot(slotId, itemStack.copy());
                    IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerValueTypeSlottedValueChangedPacket(itemStack));
                }
            } else {
                return new IRecipeTransferError() {
                    @Override
                    public Type getType() {
                        return Type.USER_FACING;
                    }

                    @Override
                    public void showError(Minecraft minecraft, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {

                    }
                };
            }
        }
        return null;
    }
}
