package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.integrateddynamics.inventory.container.ContainerVariablestore;
import org.cyclops.integrateddynamics.tileentity.TileVariablestore;

/**
 * Gui for the variablestore
 * @author rubensworks
 */
public class GuiVariablestore extends GuiContainerConfigurable<ContainerVariablestore> {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public GuiVariablestore(InventoryPlayer inventory, TileVariablestore tile) {
        super(new ContainerVariablestore(inventory, tile));
    }

    @Override
    protected ResourceLocation constructResourceLocation() {
        return new ResourceLocation(getGuiTexture());
    }

    @Override
    public String getGuiTexture() {
        return "textures/gui/container/generic_54.png";
    }

    @Override
    protected int getBaseYSize() {
        return TileVariablestore.ROWS * 18 + 17 + 96;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(guiLeft + offsetX, guiTop + offsetY, 0, 0, this.xSize, TileVariablestore.ROWS * 18 + 17);
        this.drawTexturedModalRect(guiLeft + offsetX, guiTop + offsetY + TileVariablestore.ROWS * 18 + 17, 0, 126, this.xSize, 96);
    }

}
