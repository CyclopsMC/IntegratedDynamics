package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.integrateddynamics.core.client.gui.GuiActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerMaterializer;
import org.cyclops.integrateddynamics.tileentity.TileMaterializer;

/**
 * Gui for the proxy.
 * @author rubensworks
 */
public class GuiMaterializer extends GuiActiveVariableBase<ContainerMaterializer, TileMaterializer> {

    private static final int ERROR_X = 110;
    private static final int ERROR_Y = 26;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public GuiMaterializer(InventoryPlayer inventory, TileMaterializer tile) {
        super(new ContainerMaterializer(inventory, tile));
    }

    @Override
    protected int getBaseYSize() {
        return 189;
    }

    @Override
    protected int getErrorX() {
        return ERROR_X;
    }

    @Override
    protected int getErrorY() {
        return ERROR_Y;
    }
}
