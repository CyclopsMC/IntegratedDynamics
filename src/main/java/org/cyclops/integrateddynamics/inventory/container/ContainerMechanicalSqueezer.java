package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMechanicalMachine;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalSqueezer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Container for the mechanical squeezer.
 * @author rubensworks
 */
public class ContainerMechanicalSqueezer extends ContainerMechanicalMachine<TileMechanicalSqueezer> {

    public static final String BUTTON_TOGGLE_FLUID_EJECT = "button_eject";

    private final Supplier<FluidStack> variableFluidStack;
    private final Supplier<Integer> variableFluidCapacity;
    private final Supplier<Boolean> variableAutoEject;

    public ContainerMechanicalSqueezer(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, new Inventory(TileMechanicalSqueezer.INVENTORY_SIZE), Optional.empty());
    }

    public ContainerMechanicalSqueezer(int id, PlayerInventory playerInventory, IInventory inventory,
                                          Optional<TileMechanicalSqueezer> tileSupplier) {
        super(RegistryEntries.CONTAINER_MECHANICAL_SQUEEZER, id, playerInventory, inventory, tileSupplier);

        this.variableFluidStack = registerSyncedVariable(FluidStack.class, () -> getTileSupplier().get().getTank().getFluid());
        this.variableFluidCapacity = registerSyncedVariable(Integer.class, () -> getTileSupplier().get().getTank().getCapacity());
        this.variableAutoEject = registerSyncedVariable(Boolean.class, () -> getTileSupplier().get().isAutoEjectFluids());

        addSlot(new Slot(inventory, 0, 44, 37));

        addSlot(new SlotRemoveOnly(inventory, 1, 98, 29));
        addSlot(new SlotRemoveOnly(inventory, 2, 116, 29));
        addSlot(new SlotRemoveOnly(inventory, 3, 98, 47));
        addSlot(new SlotRemoveOnly(inventory, 4, 116, 47));

        addPlayerInventory(playerInventory, offsetX + 8, offsetY + 86);

        putButtonAction(BUTTON_TOGGLE_FLUID_EJECT,
                (buttonId, container) -> getTileSupplier().ifPresent(tile -> tile.setAutoEjectFluids(!getTileSupplier().get().isAutoEjectFluids())));
    }

    @Nullable
    public FluidStack getFluidStack() {
        return variableFluidStack.get();
    }

    public int getFluidCapacity() {
        return variableFluidCapacity.get();
    }

    public boolean isAutoEjectFluids() {
        return variableAutoEject.get();
    }

}
