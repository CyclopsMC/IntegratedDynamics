package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.integrateddynamics.inventory.container.ContainerCoalGenerator;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

/**
 * Gui for the coal generator.
 * @author rubensworks
 */
public class GuiCoalGenerator extends GuiContainerConfigurable<ContainerCoalGenerator> {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public GuiCoalGenerator(InventoryPlayer inventory, TileCoalGenerator tile) {
        super(new ContainerCoalGenerator(inventory, tile));
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        int lastProgress = getContainer().getProgress();
        if (lastProgress >= 0) {
            this.drawTexturedModalRect(getGuiLeftTotal() + 81, getGuiTopTotal() + 30 + lastProgress, 176,
                    lastProgress, 14, TileCoalGenerator.MAX_PROGRESS - lastProgress + 1);
        }
    }

}
