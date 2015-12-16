package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.BlockCoalGenerator;
import org.cyclops.integrateddynamics.inventory.container.ContainerCoalGenerator;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;
import org.cyclops.integrateddynamics.tileentity.TileVariablestore;

/**
 * Gui for the variablestore
 * @author rubensworks
 */
public class GuiCoalGenerator extends GuiContainerExtended {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The tile.
     */
    public GuiCoalGenerator(InventoryPlayer inventory, TileCoalGenerator tile) {
        super(new ContainerCoalGenerator(inventory, tile));
    }

    @Override
    public String getGuiTexture() {
        return IntegratedDynamics._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI) + "coalGenerator.png";
    }

    @Override
    protected int getBaseYSize() {
        return TileVariablestore.ROWS * 18 + 17 + 96;
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(BlockCoalGenerator.getInstance().getLocalizedName(), 8, 6, 4210752);
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        ContainerCoalGenerator container = (ContainerCoalGenerator) getContainer();
        int lastProgress = container.getLastProgress();

        if (lastProgress > 0) {
            this.drawTexturedModalRect(this.guiLeft + offsetX + 81, this.guiTop + offsetY + 30 + lastProgress, 176,
                    lastProgress, 14, TileCoalGenerator.MAX_PROGRESS - lastProgress + 1);
        }
    }

}
