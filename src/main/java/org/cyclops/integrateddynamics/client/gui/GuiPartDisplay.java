package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiMultipart;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartDisplay;
import org.cyclops.integrateddynamics.part.PartTypeDisplay;


/**
 * Gui for a writer part.
 * @author rubensworks
 */
public class GuiPartDisplay extends GuiMultipart<PartTypeDisplay, PartTypeDisplay.State> {

    private static final int ERROR_X = 104;
    private static final int ERROR_Y = 16;
    private static final int OK_X = 104;
    private static final int OK_Y = 16;

    /**
     * Make a new instance.
     * @param partTarget The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The targeted part type.
     */
    public GuiPartDisplay(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, PartTypeDisplay partType) {
        super(new ContainerPartDisplay(player, partTarget, partContainer, partType));
    }

    @Override
    protected String getNameId() {
        return "partDisplay";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        IValue value = getPartState().getDisplayValue();
        if(value != null) {
            String stringValue = value.getType().toCompactString(value);
            fontRendererObj.drawString(stringValue, this.guiLeft + offsetX + 52,
                    this.guiTop + offsetY + 34, value.getType().getDisplayColor());
        }

        GlStateManager.color(1, 1, 1);
        displayErrors.drawBackground(getPartState().getGlobalErrors(), ERROR_X, ERROR_Y, OK_X, OK_Y, this,
                this.guiLeft, this.guiTop, getPartState().hasVariable());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        // Render error tooltip
        displayErrors.drawForeground(getPartState().getGlobalErrors(), ERROR_X, ERROR_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
    }

    @Override
    protected int getBaseXSize() {
        return 176;
    }

    @Override
    protected int getBaseYSize() {
        return 128;
    }
}
