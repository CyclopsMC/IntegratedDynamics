package org.cyclops.integrateddynamics.core.inventory.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.item.ItemVariable;

/**
 * Slot for a variable item.
 * @author rubensworks
 */
public class SlotVariable extends SlotSingleItem {

    public static ResourceLocation VARIABLE_EMPTY = new ResourceLocation(Reference.MOD_ID, "slots/variable_empty");

    /**
     * Make a new instance.
     *
     * @param inventory The inventory this slot will be in.
     * @param index     The index of this slot.
     * @param x         X coordinate.
     * @param y         Y coordinate.
     */
    public SlotVariable(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y, ItemVariable.getInstance());
        setBackgroundName(SlotVariable.VARIABLE_EMPTY.toString());
    }
}
