package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
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
    protected Identifier constructGuiTexture() {
        return Identifier.parse("textures/gui/container/generic_54.png");
    }

    @Override
    protected int getBaseYSize() {
        return BlockEntityVariablestore.ROWS * 18 + 17 + 96;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + offsetX, topPos + offsetY, 0, 0, this.imageWidth, BlockEntityVariablestore.ROWS * 18 + 17, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + offsetX, topPos + offsetY + BlockEntityVariablestore.ROWS * 18 + 17, 0, 126, this.imageWidth, 96, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        guiGraphics.text(font, this.title, this.titleLabelX, this.titleLabelY, ARGB.opaque(4210752), false);
    }
}
