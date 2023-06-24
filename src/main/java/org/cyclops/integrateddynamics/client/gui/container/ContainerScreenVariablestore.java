package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.inventory.container.ContainerVariablestore;

/**
 * Gui for the variablestore
 * @author rubensworks
 */
public class ContainerScreenVariablestore extends ContainerScreenExtended<ContainerVariablestore> {

    public ContainerScreenVariablestore(ContainerVariablestore container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation("textures/gui/container/generic_54.png");
    }

    @Override
    protected int getBaseYSize() {
        return BlockEntityVariablestore.ROWS * 18 + 17 + 96;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(texture, leftPos + offsetX, topPos + offsetY, 0, 0, this.imageWidth, BlockEntityVariablestore.ROWS * 18 + 17);
        guiGraphics.blit(texture, leftPos + offsetX, topPos + offsetY + BlockEntityVariablestore.ROWS * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        this.font.drawInBatch(this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752, false,
                guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }
}
