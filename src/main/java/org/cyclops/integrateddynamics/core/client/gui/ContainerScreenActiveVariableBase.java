package org.cyclops.integrateddynamics.core.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.core.client.gui.container.DisplayErrorsComponent;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerActiveVariableBase;

/**
 * Base gui for part entities that can hold variables.
 * @author rubensworks
 */
public abstract class ContainerScreenActiveVariableBase<C extends ContainerActiveVariableBase<?>> extends ContainerScreenExtended<C> {

    protected final DisplayErrorsComponent displayErrors = new DisplayErrorsComponent();

    public ContainerScreenActiveVariableBase(C container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }

    protected abstract int getErrorX();
    protected abstract int getErrorY();

    protected int getValueX() {
        return 54;
    }

    protected int getValueY() {
        return 57;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int x, int y) {
        super.renderBg(guiGraphics, f, x, y);

        Component readValue = getMenu().getReadValue();
        int readValueColor = getMenu().getReadValueColor();
        boolean ok = false;
        if (readValue != null) {
            ok = true;
            RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font, readValue.getString(),
                    getGuiLeftTotal() + getValueX(), getGuiTopTotal() + getValueY(), 70, readValueColor, false, Font.DisplayMode.NORMAL);
        }

        displayErrors.drawBackground(guiGraphics, getMenu().getReadErrors(), getErrorX(), getErrorY(), getErrorX(), getErrorY(), this,
                this.leftPos, this.topPos, ok);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        this.font.drawInBatch(this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752, false,
                guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        displayErrors.drawForeground(guiGraphics.pose(), getMenu().getReadErrors(), getErrorX(), getErrorY(), mouseX, mouseY, this, this.leftPos, this.topPos);
    }
}
