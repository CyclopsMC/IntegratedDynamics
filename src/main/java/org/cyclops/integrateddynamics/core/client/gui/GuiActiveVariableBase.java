package org.cyclops.integrateddynamics.core.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.integrateddynamics.core.client.gui.container.DisplayErrorsComponent;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerActiveVariableBase;
import org.cyclops.integrateddynamics.core.tileentity.TileActiveVariableBase;

/**
 * Base gui for part entities that can hold variables.
 * @author rubensworks
 */
public abstract class GuiActiveVariableBase<C extends ContainerActiveVariableBase<T>, T extends TileActiveVariableBase<?>> extends GuiContainerConfigurable<C> {

    protected final DisplayErrorsComponent displayErrors = new DisplayErrorsComponent();

    /**
     * Make a new instance.
     * @param container The container
     */
    public GuiActiveVariableBase(C container) {
        super(container);
    }

    protected abstract int getErrorX();
    protected abstract int getErrorY();

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
        displayErrors.drawBackground(getContainer().getTile().getErrors(), getErrorX(), getErrorY(), getErrorX(), getErrorY(), this,
                this.guiLeft, this.guiTop, ok);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        displayErrors.drawForeground(getContainer().getTile().getErrors(), getErrorX(), getErrorY(), mouseX, mouseY, this, this.guiLeft, this.guiTop);
    }
}
