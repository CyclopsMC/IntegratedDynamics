package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainerConfigurable;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;

/**
 * A base container for {@link TileMechanicalMachine}.
 * @author rubensworks
 */
public class ContainerMechanicalMachine<T extends TileMechanicalMachine<?, ?, ?, ?, ?>> extends TileInventoryContainerConfigurable<T> {

    private int lastMaxProgress;
    private int lastProgress;

    /**
     * Make a new ContainerMechanicalMachine.
     *
     * @param inventory The player inventory.
     * @param tile      The TileEntity for this container.
     */
    public ContainerMechanicalMachine(InventoryPlayer inventory, T tile) {
        super(inventory, tile);
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
