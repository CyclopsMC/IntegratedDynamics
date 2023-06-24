package org.cyclops.integrateddynamics.client.gui.container;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.helper.RenderHelpers;
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
        extends ContainerScreenMultipartAspects<P, S, IAspectWrite, ContainerPartWriter<P, S>> {

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
    protected void drawAdditionalElementInfoForeground(PoseStack matrixStack, ContainerPartWriter<P, S> container, int index, IAspectWrite aspect, int mouseX, int mouseY) {
        // Render error tooltip
        if(getMenu().isPartStateEnabled()) {
            displayErrors.drawForeground(matrixStack, getMenu().getAspectErrors(aspect), ERROR_X, ERROR_Y + container.getAspectBoxHeight() * index, mouseX, mouseY, this, this.leftPos, this.topPos);
        }
    }

    @Override
    protected void drawAdditionalElementInfo(GuiGraphics guiGraphics, ContainerPartWriter<P, S> container, int index, IAspectWrite aspect) {
        int aspectBoxHeight = container.getAspectBoxHeight();

        // Render dummy target item
        // This could be cached if this would prove to be a bottleneck
        ItemStack itemStack = container.writeAspectInfo(false, new ItemStack(RegistryEntries.ITEM_VARIABLE), container.getPlayerIInventory().player.level(), aspect);
        Rectangle pos = getElementPosition(container, index, true);
        Lighting.setupForFlatItems();
        guiGraphics.renderItem(itemStack, pos.x, pos.y);

        // Render error symbol
        if(getMenu().isPartStateEnabled()) {
            displayErrors.drawBackground(guiGraphics, getMenu().getAspectErrors(aspect), ERROR_X, ERROR_Y + aspectBoxHeight * index, OK_X, OK_Y + aspectBoxHeight * index, this,
                    this.leftPos, this.topPos, getMenu().getPartStateActiveAspect() == aspect);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        ContainerPartWriter<?, ?> container = getMenu();
        RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font, container.getWriteValue().getString(), this.leftPos + offsetX + 53,
                this.topPos + offsetY + 132, 70, container.getWriteValueColor(), false, Font.DisplayMode.NORMAL);
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
