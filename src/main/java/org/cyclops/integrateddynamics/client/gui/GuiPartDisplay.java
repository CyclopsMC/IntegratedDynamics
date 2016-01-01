package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiMultipart;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartDisplay;


/**
 * Gui for a writer part.
 * @author rubensworks
 */
public class GuiPartDisplay<P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> extends GuiMultipart<P, S> {

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
    public GuiPartDisplay(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, IPartType partType) {
        super(new ContainerPartDisplay<P, S>(player, partTarget, partContainer, partType));
    }

    @Override
    protected String getNameId() {
        return "partDisplay";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        String readValue = ((ContainerPartDisplay) getContainer()).getReadValue();
        int readValueColor = ((ContainerPartDisplay) getContainer()).getReadValueColor();
        boolean ok = false;
        if(readValue != null) {
            ok = true;
            FontRenderer fontRenderer = fontRendererObj;
            fontRenderer.drawString(readValue, getGuiLeft() + 53, getGuiTop() + 34, readValueColor);
        }

        GlStateManager.color(1, 1, 1);
        displayErrors.drawBackground(getPartState().getGlobalErrors(), ERROR_X, ERROR_Y, OK_X, OK_Y, this,
                this.guiLeft, this.guiTop, ok);
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
