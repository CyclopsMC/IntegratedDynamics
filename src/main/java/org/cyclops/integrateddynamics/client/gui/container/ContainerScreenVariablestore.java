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
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        this.getMinecraft().getTextureManager().bindTexture(texture);
        this.blit(matrixStack, guiLeft + offsetX, guiTop + offsetY, 0, 0, this.xSize, TileVariablestore.ROWS * 18 + 17);
        this.blit(matrixStack, guiLeft + offsetX, guiTop + offsetY + TileVariablestore.ROWS * 18 + 17, 0, 126, this.xSize, 96);
    }

}
