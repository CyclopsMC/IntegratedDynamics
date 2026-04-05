package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.blockentity.BlockEntityCoalGenerator;
import org.cyclops.integrateddynamics.inventory.container.ContainerCoalGenerator;

/**
 * Gui for the coal generator.
 * @author rubensworks
 */
public class ContainerScreenCoalGenerator extends ContainerScreenExtended<ContainerCoalGenerator> {

    public ContainerScreenCoalGenerator(ContainerCoalGenerator container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/coal_generator.png");
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int lastProgress = getMenu().getProgress();
        if (lastProgress >= 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getGuiTexture(), getGuiLeftTotal() + 81, getGuiTopTotal() + 30 + lastProgress, 176,
                    lastProgress, 14, BlockEntityCoalGenerator.MAX_PROGRESS - lastProgress + 1, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        guiGraphics.text(font, this.title, this.titleLabelX, this.titleLabelY, ARGB.opaque(4210752), false);
    }
}
