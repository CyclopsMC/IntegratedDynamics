package org.cyclops.integrateddynamics.core.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.IModHelpers;
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
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);

        Component readValue = getMenu().getReadValue();
        int readValueColor = getMenu().getReadValueColor();
        boolean ok = false;
        if (readValue != null) {
            ok = true;
            IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, font, readValue.getString(),
                    getGuiLeftTotal() + getValueX(), getGuiTopTotal() + getValueY(), 70, ARGB.opaque(readValueColor), false, Font.DisplayMode.NORMAL);
        }

        displayErrors.drawBackground(guiGraphics, getMenu().getReadErrors(), getErrorX(), getErrorY(), getErrorX(), getErrorY(), this,
                this.leftPos, this.topPos, ok);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        guiGraphics.text(font, this.title, this.titleLabelX, this.titleLabelY, ARGB.opaque(4210752), false);
        displayErrors.drawForeground(guiGraphics, getMenu().getReadErrors(), getErrorX(), getErrorY(), mouseX, mouseY, this, this.leftPos, this.topPos);
    }
}
