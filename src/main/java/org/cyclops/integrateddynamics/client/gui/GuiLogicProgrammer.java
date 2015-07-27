package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammer;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammerConfig;
import org.cyclops.integrateddynamics.core.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

/**
 * Gui for the {@link org.cyclops.integrateddynamics.block.BlockLogicProgrammer}.
 * @author rubensworks
 */
public class GuiLogicProgrammer extends ScrollingGuiContainer {

    /**
     * Make a new instance.
     */
    public GuiLogicProgrammer(InventoryPlayer inventoryPlayer) {
        super(new ContainerLogicProgrammer(inventoryPlayer));
    }

    protected int getScrollX() {
        return 5;
    }

    protected int getScrollY() {
        return 18;
    }

    protected int getScrollHeight() {
        return 178;
    }

    @Override
    protected int getBaseXSize() {
        return 256;
    }

    @Override
    protected int getBaseYSize() {
        return 213;
    }

    @Override
    protected int getSearchX() {
        return 6;
    }

    protected int getSearchWidth() {
        return 70;
    }

    @Override
    public String getGuiTexture() {
        return getContainer().getGuiProvider().getMod().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + BlockLogicProgrammerConfig._instance.getNamedId() + ".png";
    }

    protected float colorSmoothener(float color) {
        return 1F - ((1F - color) / 4F);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        FontRenderer fontRenderer = fontRendererObj;

        // Draw container name
        fontRenderer.drawString(L10NHelpers.localize(BlockLogicProgrammer.getInstance().getLocalizedName()),
                this.guiLeft + offsetX + 87, this.guiTop + offsetY + 7, Helpers.RGBToInt(80, 80, 80));

        // Draw aspects
        ContainerLogicProgrammer container = (ContainerLogicProgrammer) getScrollingInventoryContainer();
        int boxHeight = 18;
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                IOperator operator = container.getVisibleElement(i);

                GlStateManager.disableAlpha();
                Triple<Float, Float, Float> rgb = Helpers.intToRGB(operator.getOutputType().getDisplayColor());
                GlStateManager.color(colorSmoothener(rgb.getLeft()), colorSmoothener(rgb.getMiddle()),
                        colorSmoothener(rgb.getRight()), 1);

                // Background
                mc.renderEngine.bindTexture(texture);
                drawTexturedModalRect(guiLeft + offsetX + 19,
                        guiTop + offsetY + 18 + boxHeight * i, 19, 18, 56, boxHeight - 1);

                // Aspect type info
                // TODO: tooltips
                String aspectName = L10NHelpers.localize(operator.getSymbol());
                RenderHelpers.drawScaledCenteredString(fontRenderer, aspectName,
                        this.guiLeft + offsetX + 18,
                        this.guiTop + offsetY + 26 + boxHeight * i,
                        60, Helpers.RGBToInt(40, 40, 40));
            }
        }
    }

}
