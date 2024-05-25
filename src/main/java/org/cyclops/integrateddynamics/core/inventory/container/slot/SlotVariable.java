package org.cyclops.integrateddynamics.core.inventory.container.slot;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Slot for a variable item.
 * @author rubensworks
 */
public class SlotVariable extends SlotSingleItem {

    public static ResourceLocation VARIABLE_EMPTY = new ResourceLocation(Reference.MOD_ID, "slot/variable_empty");

    /**
     * Make a new instance.
     *
     * @param inventory The inventory this slot will be in.
     * @param index     The index of this slot.
     * @param x         X coordinate.
     * @param y         Y coordinate.
     */
    public SlotVariable(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y, RegistryEntries.ITEM_VARIABLE.get());
        DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> setBackground(TextureAtlas.LOCATION_BLOCKS, SlotVariable.VARIABLE_EMPTY));
    }
}
