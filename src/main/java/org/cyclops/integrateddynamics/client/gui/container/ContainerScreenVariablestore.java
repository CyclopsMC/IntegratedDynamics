package org.cyclops.integrateddynamics.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.RenderHelpers;
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
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderHelpers.bindTexture(texture);
        this.blit(matrixStack, leftPos + offsetX, topPos + offsetY, 0, 0, this.imageWidth, BlockEntityVariablestore.ROWS * 18 + 17);
        this.blit(matrixStack, leftPos + offsetX, topPos + offsetY + BlockEntityVariablestore.ROWS * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        this.font.draw(matrixStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
    }
}
