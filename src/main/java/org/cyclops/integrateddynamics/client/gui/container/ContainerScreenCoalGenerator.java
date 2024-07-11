package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/coal_generator.png");
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        int lastProgress = getMenu().getProgress();
        if (lastProgress >= 0) {
            guiGraphics.blit(getGuiTexture(), getGuiLeftTotal() + 81, getGuiTopTotal() + 30 + lastProgress, 176,
                    lastProgress, 14, BlockEntityCoalGenerator.MAX_PROGRESS - lastProgress + 1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        this.font.drawInBatch(this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752, false,
                guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }
}
