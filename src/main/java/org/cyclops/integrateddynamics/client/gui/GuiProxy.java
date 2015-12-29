package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.integrateddynamics.inventory.container.ContainerProxy;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * Gui for the proxy.
 * @author rubensworks
 */
public class GuiProxy extends GuiContainerConfigurable<ContainerProxy> {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The tile.
     */
    public GuiProxy(InventoryPlayer inventory, TileProxy tile) {
        super(new ContainerProxy(inventory, tile));
    }

    @Override
    protected int getBaseYSize() {
        return 189;
    }
}
