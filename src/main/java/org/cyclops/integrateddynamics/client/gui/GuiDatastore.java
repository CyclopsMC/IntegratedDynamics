package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.integrateddynamics.block.BlockDatastore;
import org.cyclops.integrateddynamics.inventory.container.ContainerDatastore;
import org.cyclops.integrateddynamics.tileentity.TileDatastore;

/**
 * Gui for the datastore
 * @author rubensworks
 */
public class GuiDatastore extends GuiContainerExtended {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The tile.
     */
    public GuiDatastore(InventoryPlayer inventory, TileDatastore tile) {
        super(new ContainerDatastore(inventory, tile));
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
        return TileDatastore.ROWS * 18 + 17 + 96;
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(BlockDatastore.getInstance().getLocalizedName(), 8, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(guiLeft + offsetX, guiTop + offsetY, 0, 0, this.xSize, TileDatastore.ROWS * 18 + 17);
        this.drawTexturedModalRect(guiLeft + offsetX, guiTop + offsetY + TileDatastore.ROWS * 18 + 17, 0, 126, this.xSize, 96);
    }

}
