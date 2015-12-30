package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.integrateddynamics.core.client.gui.container.DisplayErrorsComponent;
import org.cyclops.integrateddynamics.inventory.container.ContainerProxy;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * Gui for the proxy.
 * @author rubensworks
 */
public class GuiProxy extends GuiContainerConfigurable<ContainerProxy> {

    private static final int ERROR_X = 110;
    private static final int ERROR_Y = 26;
    private static final int OK_X = 110;
    private static final int OK_Y = 26;

    protected final DisplayErrorsComponent displayErrors = new DisplayErrorsComponent();

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

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);

        String readValue = getContainer().getReadValue();
        int readValueColor = getContainer().getReadValueColor();
        boolean ok = false;
        if(getContainer().getTile().hasVariable() && readValue != null) {
            ok = true;
            FontRenderer fontRenderer = fontRendererObj;
            fontRenderer.drawString(readValue, getGuiLeft() + 53, getGuiTop() + 53, readValueColor);
        }

        GlStateManager.color(1, 1, 1);
        displayErrors.drawBackground(getContainer().getTile().getErrors(), ERROR_X, ERROR_Y, OK_X, OK_Y, this,
                this.guiLeft, this.guiTop, ok);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        displayErrors.drawForeground(getContainer().getTile().getErrors(), ERROR_X, ERROR_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
    }
}
