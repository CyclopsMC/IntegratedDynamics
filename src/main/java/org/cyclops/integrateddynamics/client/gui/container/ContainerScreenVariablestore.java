package org.cyclops.integrateddynamics.client.gui.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.integrateddynamics.inventory.container.ContainerVariablestore;
import org.cyclops.integrateddynamics.tileentity.TileVariablestore;

/**
 * Gui for the variablestore
 * @author rubensworks
 */
public class ContainerScreenVariablestore extends ContainerScreenExtended<ContainerVariablestore> {

    public ContainerScreenVariablestore(ContainerVariablestore container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation("textures/gui/container/generic_54.png");
    }

    @Override
    protected int getBaseYSize() {
        return TileVariablestore.ROWS * 18 + 17 + 96;
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        this.getMinecraft().getTextureManager().bind(texture);
        this.blit(matrixStack, leftPos + offsetX, topPos + offsetY, 0, 0, this.imageWidth, TileVariablestore.ROWS * 18 + 17);
        this.blit(matrixStack, leftPos + offsetX, topPos + offsetY + TileVariablestore.ROWS * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        this.font.draw(matrixStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
    }
}
