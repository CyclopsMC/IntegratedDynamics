package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainerConfigurable;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
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
            String readValue = "";
            int readValueColor = 0;
            IVariable variable = getTile().getVariable(NetworkHelpers.getPartNetwork(getTile().getNetwork()));
            if(variable != null) {
                try {
                    IValue value = variable.getValue();
                    readValue = value.getType().toCompactString(value);
                    readValueColor = variable.getType().getDisplayColor();
                } catch (EvaluationException | NullPointerException e) {
                    readValue = "ERROR";
                    readValueColor = Helpers.RGBToInt(255, 0, 0);
                }
            }
            ValueNotifierHelpers.setValue(this, readValueId, readValue);
            ValueNotifierHelpers.setValue(this, readColorId, readValueColor);
        }
    }

    public String getReadValue() {
        return ValueNotifierHelpers.getValueString(this, readValueId);
    }

    public int getReadValueColor() {
        return ValueNotifierHelpers.getValueInt(this, readColorId);
    }

}
