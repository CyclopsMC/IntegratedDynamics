package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainerConfigurable;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalSqueezer;

/**
 * Container for the mechanical squeezer.
 * @author rubensworks
 */
public class ContainerMechanicalSqueezer extends TileInventoryContainerConfigurable<TileMechanicalSqueezer> {

    public static final int BUTTON_TOGGLE_FLUID_EJECT = 0;

    private int lastMaxProgress;
    private int lastProgress;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public ContainerMechanicalSqueezer(InventoryPlayer inventory, TileMechanicalSqueezer tile) {
        super(inventory, tile);

        addSlotToContainer(new Slot(tile, 0, 44, 37));

        addSlotToContainer(new SlotRemoveOnly(tile, 1, 98, 29));
        addSlotToContainer(new SlotRemoveOnly(tile, 2, 116, 29));
        addSlotToContainer(new SlotRemoveOnly(tile, 3, 98, 47));
        addSlotToContainer(new SlotRemoveOnly(tile, 4, 116, 47));

        addPlayerInventory(inventory, offsetX + 8, offsetY + 86);

        putButtonAction(BUTTON_TOGGLE_FLUID_EJECT,
                (buttonId, container) -> getTile().setAutoEjectFluids(!getTile().isAutoEjectFluids()));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < this.listeners.size(); ++i) {
            IContainerListener crafting = this.listeners.get(i);
            if(lastMaxProgress != getTile().getMaxProgress()) {
                crafting.sendWindowProperty(this, 0, getTile().getMaxProgress());
            }
            if(lastProgress != getTile().getProgress()) {
                crafting.sendWindowProperty(this, 1, getTile().getProgress());
            }
        }
        this.lastProgress = getTile().getProgress();
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        if(id == 0) {
            this.lastMaxProgress = data;
        }
        if(id == 1) {
            this.lastProgress = data;
        }
    }

    public int getLastMaxProgress() {
        return this.lastMaxProgress;
    }

    public int getLastProgress() {
        return this.lastProgress;
    }

}
