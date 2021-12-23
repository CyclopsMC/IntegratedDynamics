package org.cyclops.integrateddynamics.core.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
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

    public ContainerScreenActiveVariableBase(C container, PlayerInventory playerInventory, ITextComponent title) {
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
    protected void renderBg(MatrixStack matrixStack, float f, int x, int y) {
        // super.renderBg(matrixStack, f, x, y); // TODO: restore

        ITextComponent readValue = getMenu().getReadValue();
        int readValueColor = getMenu().getReadValueColor();
        boolean ok = false;
        if (readValue != null) {
            ok = true;
            RenderHelpers.drawScaledCenteredString(matrixStack, font, readValue.getString(),
                    getGuiLeftTotal() + getValueX(), getGuiTopTotal() + getValueY(), 70, readValueColor);
        }

        RenderSystem.color3f(1, 1, 1);
        displayErrors.drawBackground(matrixStack, getMenu().getReadErrors(), getErrorX(), getErrorY(), getErrorX(), getErrorY(), this,
                this.leftPos, this.topPos, ok);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        this.font.draw(matrixStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        displayErrors.drawForeground(matrixStack, getMenu().getReadErrors(), getErrorX(), getErrorY(), mouseX, mouseY, this, this.leftPos, this.topPos);
    }
}
