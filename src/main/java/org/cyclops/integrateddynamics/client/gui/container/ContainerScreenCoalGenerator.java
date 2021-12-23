package org.cyclops.integrateddynamics.client.gui.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.inventory.container.ContainerCoalGenerator;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

/**
 * Gui for the coal generator.
 * @author rubensworks
 */
public class ContainerScreenCoalGenerator extends ContainerScreenExtended<ContainerCoalGenerator> {

    public ContainerScreenCoalGenerator(ContainerCoalGenerator container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/coal_generator.png");
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        // super.renderBg(matrixStack, partialTicks, mouseX, mouseY); // TODO: restore
        int lastProgress = getMenu().getProgress();
        if (lastProgress >= 0) {
            this.blit(matrixStack, getGuiLeftTotal() + 81, getGuiTopTotal() + 30 + lastProgress, 176,
                    lastProgress, 14, TileCoalGenerator.MAX_PROGRESS - lastProgress + 1);
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        this.font.draw(matrixStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
    }
}
