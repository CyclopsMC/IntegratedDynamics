package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalDryingBasin;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMechanicalMachine;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Container for the mechanical drying basin.
 * @author rubensworks
 */
public class ContainerMechanicalDryingBasin extends ContainerMechanicalMachine<BlockEntityMechanicalDryingBasin> {

    private final Supplier<FluidStack> variableInputFluidStack;
    private final Supplier<Integer> variableInputFluidCapacity;
    private final Supplier<FluidStack> variableOutputFluidStack;
    private final Supplier<Integer> variableOutputFluidCapacity;

    public ContainerMechanicalDryingBasin(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(BlockEntityMechanicalDryingBasin.INVENTORY_SIZE), Optional.empty());
    }

    public ContainerMechanicalDryingBasin(int id, Inventory playerInventory, Container inventory,
                                          Optional<BlockEntityMechanicalDryingBasin> tileSupplier) {
        super(RegistryEntries.CONTAINER_MECHANICAL_DRYING_BASIN, id, playerInventory, inventory, tileSupplier);

        this.variableInputFluidStack = registerSyncedVariable(FluidStack.class, () -> getTileSupplier().get().getTankInput().getFluid());
        this.variableInputFluidCapacity = registerSyncedVariable(Integer.class, () -> getTileSupplier().get().getTankInput().getCapacity());
        this.variableOutputFluidStack = registerSyncedVariable(FluidStack.class, () -> getTileSupplier().get().getTankOutput().getFluid());
        this.variableOutputFluidCapacity = registerSyncedVariable(Integer.class, () -> getTileSupplier().get().getTankOutput().getCapacity());

        addSlot(new Slot(inventory, 0, 54, 37));

        addSlot(new SlotRemoveOnly(inventory, 1, 108, 29));
        addSlot(new SlotRemoveOnly(inventory, 2, 126, 29));
        addSlot(new SlotRemoveOnly(inventory, 3, 108, 47));
        addSlot(new SlotRemoveOnly(inventory, 4, 126, 47));

        addPlayerInventory(playerInventory, offsetX + 8, offsetY + 86);
    }

    @Nullable
    public FluidStack getInputFluidStack() {
        return variableInputFluidStack.get();
    }

    public int getInputFluidCapacity() {
        return variableInputFluidCapacity.get();
    }

    @Nullable
    public FluidStack getOutputFluidStack() {
        return variableOutputFluidStack.get();
    }

    public int getOutputFluidCapacity() {
        return variableOutputFluidCapacity.get();
    }
}
