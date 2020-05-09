package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainerConfigurable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileActiveVariableBase;

/**
 * Base container for part entities that can hold variables.
 * @author rubensworks
 */
public class ContainerActiveVariableBase<T extends TileActiveVariableBase<?>> extends TileInventoryContainerConfigurable<T> {

    private final int readValueId;
    private final int readColorId;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public ContainerActiveVariableBase(InventoryPlayer inventory, T tile) {
        super(inventory, tile);
        readValueId = getNextValueId();
        readColorId = getNextValueId();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if(!MinecraftHelpers.isClientSide()) {
            IVariable variable = getTile().getVariable(NetworkHelpers.getPartNetwork(getTile().getNetwork()));
            Pair<String, Integer> readValue = ValueHelpers.getSafeReadableValue(variable);
            ValueNotifierHelpers.setValue(this, readValueId, readValue.getLeft());
            ValueNotifierHelpers.setValue(this, readColorId, readValue.getRight());
        }
    }

    public String getReadValue() {
        return ValueNotifierHelpers.getValueString(this, readValueId);
    }

    public int getReadValueColor() {
        return ValueNotifierHelpers.getValueInt(this, readColorId);
    }

}
