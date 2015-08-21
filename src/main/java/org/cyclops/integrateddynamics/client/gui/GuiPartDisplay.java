package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartDisplay;
import org.cyclops.integrateddynamics.part.PartTypeDisplay;


/**
 * Gui for a writer part.
 * @author rubensworks
 */
public class GuiPartDisplay extends GuiMultipart<PartTypeDisplay, PartTypeDisplay.State> {

    private static final int ERROR_X = 152;
    private static final int ERROR_Y = 20;
    private static final int OK_X = 152;
    private static final int OK_Y = 20;

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

        // TODO: improve

        ContainerPartDisplay container = (ContainerPartDisplay) getContainer();
        fontRendererObj.drawString(container.getWriteValue(), this.guiLeft + offsetX + 53,
                this.guiTop + offsetY + 128, container.getWriteValueColor());
    }

    @Override
    protected int getBaseXSize() {
        return 176;
    }

    @Override
    protected int getBaseYSize() {
        return 113;
    }
}
