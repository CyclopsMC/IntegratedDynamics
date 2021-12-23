package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileActiveVariableBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Base container for part entities that can hold variables.
 * @author rubensworks
 */
public class ContainerActiveVariableBase<T extends TileActiveVariableBase> extends InventoryContainer {

    private final Optional<T> tileSupplier;
    private final int readValueId;
    private final int readColorId;
    private final int readErrorsId;

    public ContainerActiveVariableBase(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory,
                                       IInventory inventory, Optional<T> tileSupplier) {
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
                        Pair<IFormattableTextComponent, Integer> readValue = ValueHelpers.getSafeReadableValue(variable);
                        ValueNotifierHelpers.setValue(this, readValueId, readValue.getLeft());
                        ValueNotifierHelpers.setValue(this, readColorId, readValue.getRight());
                        ValueNotifierHelpers.setValue(this, readErrorsId, tile.getEvaluator().getErrors());
                    });
        });
    }

    public Optional<T> getTileSupplier() {
        return tileSupplier;
    }

    public ITextComponent getReadValue() {
        return ValueNotifierHelpers.getValueTextComponent(this, readValueId);
    }

    public int getReadValueColor() {
        return ValueNotifierHelpers.getValueInt(this, readColorId);
    }

    public List<IFormattableTextComponent> getReadErrors() {
        return ValueNotifierHelpers.getValueTextComponentList(this, readErrorsId);
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return false; // TODO: rm
    }

}
