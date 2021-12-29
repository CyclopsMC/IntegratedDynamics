package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityActiveVariableBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Base container for part entities that can hold variables.
 * @author rubensworks
 */
public class ContainerActiveVariableBase<T extends BlockEntityActiveVariableBase> extends InventoryContainer {

    private final Optional<T> tileSupplier;
    private final int readValueId;
    private final int readColorId;
    private final int readErrorsId;

    public ContainerActiveVariableBase(@Nullable MenuType<?> type, int id, Inventory playerInventory,
                                       Container inventory, Optional<T> tileSupplier) {
        super(type, id, playerInventory, inventory);
        this.tileSupplier = tileSupplier;
        this.readValueId = getNextValueId();
        this.readColorId = getNextValueId();
        this.readErrorsId = getNextValueId();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        tileSupplier.ifPresent(tile -> {
            NetworkHelpers.getPartNetwork(tile.getNetwork())
                    .ifPresent(partNetwork -> {
                        IVariable variable = tile.getVariable(partNetwork);
                        Pair<MutableComponent, Integer> readValue = ValueHelpers.getSafeReadableValue(variable);
                        ValueNotifierHelpers.setValue(this, readValueId, readValue.getLeft());
                        ValueNotifierHelpers.setValue(this, readColorId, readValue.getRight());
                        ValueNotifierHelpers.setValue(this, readErrorsId, tile.getEvaluator().getErrors());
                    });
        });
    }

    public Optional<T> getTileSupplier() {
        return tileSupplier;
    }

    public Component getReadValue() {
        return ValueNotifierHelpers.getValueTextComponent(this, readValueId);
    }

    public int getReadValueColor() {
        return ValueNotifierHelpers.getValueInt(this, readColorId);
    }

    public List<MutableComponent> getReadErrors() {
        return ValueNotifierHelpers.getValueTextComponentList(this, readErrorsId);
    }

}
