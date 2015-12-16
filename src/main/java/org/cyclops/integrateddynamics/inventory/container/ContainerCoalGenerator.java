package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainer;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

/**
 * Container for the coal generator.
 * @author rubensworks
 */
public class ContainerCoalGenerator extends TileInventoryContainer<TileCoalGenerator> {

    private int lastProgress;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The tile.
     */
    public ContainerCoalGenerator(InventoryPlayer inventory, TileCoalGenerator tile) {
        super(inventory, tile);
        addInventory(tile, 0, offsetX + 80, offsetY + 11, 1, 1);
        addPlayerInventory(inventory, offsetX + 8, offsetY + 46);
    }

    @Override
    public Slot createNewSlot(IInventory inventory, int index, int row, int column) {
        if(inventory instanceof InventoryPlayer) {
            return super.createNewSlot(inventory, index, row, column);
        }
        return new SlotFurnaceFuel(inventory, index, row, column);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < this.crafters.size(); ++i) {
            ICrafting crafting = this.crafters.get(i);
            if(lastProgress != getTile().getProgress()) {
                crafting.sendProgressBarUpdate(this, 0, getTile().getProgress());
            }
        }
        this.lastProgress = getTile().getProgress();
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        if(id == 0) {
            this.lastProgress = data;
        }
    }

    public int getLastProgress() {
        return this.lastProgress;
    }

}
