package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainerConfigurable;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;

import java.util.function.Supplier;

/**
 * A base container for {@link TileMechanicalMachine}.
 * @author rubensworks
 */
public class ContainerMechanicalMachine<T extends TileMechanicalMachine<?, ?, ?, ?, ?>> extends TileInventoryContainerConfigurable<T> {

    private final Supplier<Integer> variableMaxProgress;
    private final Supplier<Integer> variableProgress;
    private final Supplier<Integer> variableMaxEnergy;
    private final Supplier<Integer> variableEnergy;

    /**
     * Make a new ContainerMechanicalMachine.
     *
     * @param inventory The player inventory.
     * @param tile      The TileEntity for this container.
     */
    public ContainerMechanicalMachine(InventoryPlayer inventory, T tile) {
        super(inventory, tile);
        this.variableMaxProgress = registerSyncedVariable(Integer.class, () -> getTile().getMaxProgress());
        this.variableProgress = registerSyncedVariable(Integer.class, () -> getTile().getProgress());
        this.variableMaxEnergy = registerSyncedVariable(Integer.class, () -> getTile().getMaxEnergyStored());
        this.variableEnergy = registerSyncedVariable(Integer.class, () -> getTile().getEnergyStored());
    }

    public int getMaxProgress() {
        return variableMaxProgress.get();
    }

    public int getProgress() {
        return variableProgress.get();
    }

    public int getMaxEnergy() {
        return variableMaxEnergy.get();
    }

    public int getEnergy() {
        return variableEnergy.get();
    }
}
