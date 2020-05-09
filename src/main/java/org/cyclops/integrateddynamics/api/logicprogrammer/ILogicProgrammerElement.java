package org.cyclops.integrateddynamics.api.logicprogrammer;

import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.inventory.slot.SlotExtended;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

/**
 * An element instantiation inside the logic programmer.
 * @param <G> The type of gui.
 * @param <C> The type of container.
 * @param <S> The sub gui box type.
 * @author rubensworks
 */
public interface ILogicProgrammerElement<S extends ISubGuiBox, G extends Gui, C extends Container> extends IGuiInputElement<S, G, C> {

    /**
     * @return The element type.
     */
    public ILogicProgrammerElementType getType();

    /**
     * @return The string used to match regex searching.
     */
    public String getMatchString();

    /**
     * If the given value type matches with this element's input.
     * @param valueType The value type to match.
     * @return If it matches
     */
    public boolean matchesInput(IValueType<?> valueType);

    /**
     * If the given value type matches with this element's output.
     * @param valueType The value type to match.
     * @return If it matches
     */
    public boolean matchesOutput(IValueType<?> valueType);

    /**
     * Create a temporary input slot for this element.
     * The number of slots depends on the provided render pattern
     * return by {@link #getRenderPattern()}.
     * @param temporaryInputSlots The inventory behind this slot.
     * @param slotId The slot id.
     * @param x The X position for this slot.
     * @param y The Y position for this slot.
     * @return The created slot.
     */
    default Slot createSlot(IInventory temporaryInputSlots, int slotId, int x, int y) {
        return createSlotDefault(this, temporaryInputSlots, slotId, x, y);
    }

    public static Slot createSlotDefault(ILogicProgrammerElement logicProgrammerElement, IInventory temporaryInputSlots, int slotId, int x, int y) {
        SlotExtended slot = new SlotExtended(temporaryInputSlots, slotId, x, y) {
            @Override
            public boolean isItemValid(ItemStack itemStack) {
                return logicProgrammerElement.isItemValidForSlot(slotId, itemStack);
            }
        };
        slot.setPhantom(true);
        return slot;
    }

    /**
     * Called when an input item slot has been updated.
     * @param slotId The slot id.
     * @param itemStack The itemstack currently in the slot, can be null.
     */
    public void onInputSlotUpdated(int slotId, ItemStack itemStack);

    /**
     * @return If this element can be written to an item in its current state.
     */
    public boolean canWriteElementPre();

    /**
     * The stack to write the current state of this element to.
     * @param player The player that is writing the element.
     * @param itemStack The stack to write to.
     * @return The resulting itemstack.
     */
    public ItemStack writeElement(EntityPlayer player, ItemStack itemStack);

    /**
     * If this element in its current state can be deactivated because of another item being inserted into the
     * write slot.
     * @return If this element can be deactivated.
     */
    public boolean canCurrentlyReadFromOtherItem();

    /**
     * @param variableFacade A variable facade
     * @return If this element corresponds to the given variable facade.
     */
    public boolean isFor(IVariableFacade variableFacade);

    /**
     * Check if the given item can be inserted into the given slot.
     * @param slotId The slot id.
     * @param itemStack The item that will be inserted.
     * @return If it can be inserted.
     */
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack);

    /**
     * Called when a player clicks on a slot.
     * @param slotId The slot id.
     * @param slot The slot.
     * @param mouseButton The mouse buttong id.
     * @param clickType The click type.
     * @param player The clicking player.
     * @return If further processing of the clicking should stop.
     */
    boolean slotClick(int slotId, Slot slot, int mouseButton, ClickType clickType, EntityPlayer player);

    /**
     * @return The max stacksize.
     */
    public int getItemStackSizeLimit();

    /**
     * @param subGui The corresponding sub gui of this element.
     * @return If this element has the active focus. For typing and things like that.
     */
    @SideOnly(Side.CLIENT)
    public boolean isFocused(S subGui);

    /**
     * Set the focus of this element.
     * @param subGui The corresponding sub gui of this element.
     * @param focused If it must be focused.
     */
    @SideOnly(Side.CLIENT)
    public void setFocused(S subGui, boolean focused);
}
