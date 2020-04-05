package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A base container for {@link TileMechanicalMachine}.
 * @author rubensworks
 */
public class ContainerMechanicalMachine<T extends TileMechanicalMachine<?, ?>> extends InventoryContainer {

    private final Optional<T> tileSupplier;
    private final Supplier<Integer> variableMaxProgress;
    private final Supplier<Integer> variableProgress;
    private final Supplier<Integer> variableMaxEnergy;
    private final Supplier<Integer> variableEnergy;

    public ContainerMechanicalMachine(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory,
                                      IInventory inventory, Optional<T> tileSupplier) {
        super(type, id, playerInventory, inventory);
        this.tileSupplier = tileSupplier;
        this.variableMaxProgress = registerSyncedVariable(Integer.class, () -> getTileSupplier().get().getMaxProgress());
        this.variableProgress = registerSyncedVariable(Integer.class, () -> getTileSupplier().get().getProgress());
        this.variableMaxEnergy = registerSyncedVariable(Integer.class, () -> getTileSupplier().get().getMaxEnergyStored());
        this.variableEnergy = registerSyncedVariable(Integer.class, () -> getTileSupplier().get().getEnergyStored());
    }

    public Optional<T> getTileSupplier() {
        return tileSupplier;
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
