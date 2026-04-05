package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenMultipartAspects;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartWriter;

import java.awt.*;


/**
 * Gui for a writer part.
 * @author rubensworks
 */
public class ContainerScreenPartWriter<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>>
        extends ContainerScreenMultipartAspects<P, S, IAspectWrite<?, ?>, ContainerPartWriter<P, S>> {

    private static final int ERROR_X = 152;
    private static final int ERROR_Y = 20;
    private static final int OK_X = 152;
    private static final int OK_Y = 20;

    public ContainerScreenPartWriter(ContainerPartWriter<P, S> container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected String getNameId() {
        return "part_writer";
    }

    @Override
    protected void drawAdditionalElementInfoForeground(GuiGraphicsExtractor guiGraphics, ContainerPartWriter<P, S> container, int index, IAspectWrite<?, ?> aspect, int mouseX, int mouseY) {
        // Render error tooltip
        if(getMenu().isPartStateEnabled()) {
            displayErrors.drawForeground(guiGraphics, getMenu().getAspectErrors(aspect), ERROR_X, ERROR_Y + container.getAspectBoxHeight() * index, mouseX, mouseY, this, this.leftPos, this.topPos);
        }
    }

    @Override
    protected void drawAdditionalElementInfo(GuiGraphicsExtractor guiGraphics, ContainerPartWriter<P, S> container, int index, IAspectWrite<?, ?> aspect) {
        int aspectBoxHeight = container.getAspectBoxHeight();

        // Render dummy target item
        // This could be cached if this would prove to be a bottleneck
        ItemStack itemStack = container.writeAspectInfo(false, new ItemStack(RegistryEntries.ITEM_VARIABLE), container.getPlayerIInventory().player.level(), aspect);
        Rectangle pos = getElementPosition(container, index, true);
        guiGraphics.item(itemStack, pos.x, pos.y);

        // Render error symbol
        if(getMenu().isPartStateEnabled()) {
            displayErrors.drawBackground(guiGraphics, getMenu().getAspectErrors(aspect), ERROR_X, ERROR_Y + aspectBoxHeight * index, OK_X, OK_Y + aspectBoxHeight * index, this,
                    this.leftPos, this.topPos, getMenu().getPartStateActiveAspect() == aspect);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        ContainerPartWriter<?, ?> container = getMenu();
        IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, font, container.getWriteValue().getString(), this.leftPos + offsetX + 53,
                this.topPos + offsetY + 132, 70, ARGB.opaque(container.getWriteValueColor()), false, Font.DisplayMode.NORMAL);
    }

    @Override
    protected int getBaseXSize() {
        return 195;
    }

    @Override
    protected int getBaseYSize() {
        return 222;
    }

    @Override
    public int getMaxLabelWidth() {
        return 85;
    }
}
