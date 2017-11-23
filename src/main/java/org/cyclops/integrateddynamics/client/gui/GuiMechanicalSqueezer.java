package org.cyclops.integrateddynamics.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonImage;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.item.DamageIndicatedItemComponent;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalSqueezer;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalSqueezer;

/**
 * Gui for the mechanical squeezer.
 * @author rubensworks
 */
public class GuiMechanicalSqueezer extends GuiContainerConfigurable<ContainerMechanicalSqueezer> {

    private final IImage imageArrowDownEnabled;
    private final IImage imageArrowDownDisabled;
    private GuiButtonImage buttonToggleFluidEject;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public GuiMechanicalSqueezer(InventoryPlayer inventory, TileMechanicalSqueezer tile) {
        super(new ContainerMechanicalSqueezer(inventory, tile));
        imageArrowDownEnabled = new Image(texture, 176, 138, 20, 10);
        imageArrowDownDisabled = new Image(texture, 176, 148, 20, 10);
    }

    @Override
    public void initGui() {
        super.initGui();

        addButton(buttonToggleFluidEject = new GuiButtonImage(ContainerMechanicalSqueezer.BUTTON_TOGGLE_FLUID_EJECT,
                getGuiLeft() + 149, getGuiTop() + 71, imageArrowDownDisabled));
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        // Update the image in the fluid eject toggle button
        buttonToggleFluidEject.setImage(getContainer().getTile().isAutoEjectFluids()
                ? imageArrowDownEnabled : imageArrowDownDisabled);

        // Render progress
        int lastMaxProgress = getContainer().getLastMaxProgress();
        int lastProgress = getContainer().getLastProgress();
        if (lastMaxProgress > 0) {
            this.drawTexturedModalRect(getGuiLeft() + 73, getGuiTop() + 36, 176,
                    120, 12, 18 * lastProgress / lastMaxProgress);
        }

        // Render energy level
        int energyStored = getContainer().getTile().getEnergyStored();
        int energyMax = getContainer().getTile().getMaxEnergyStored();
        if (energyMax > 0) {
            int height = 60 * energyStored / energyMax;
            this.drawTexturedModalRect(getGuiLeft() + 8, getGuiTop() + 16 + (60 - height), 176,
                    120 - height, 18, height);
        }

        // Render fluid tank
        FluidStack fluidStack = getContainer().getTile().getTank().getFluid();
        int tankSize = getContainer().getTile().getTank().getCapacity();
        if (fluidStack != null && tankSize > 0) {
            int level = 60 * fluidStack.amount / tankSize;
            TextureAtlasSprite icon = RenderHelpers.getFluidIcon(fluidStack, EnumFacing.UP);
            int verticalOffset = 0;
            while(level > 0) {
                int textureHeight;
                if(level > 16) {
                    textureHeight = 16;
                    level -= 16;
                } else {
                    textureHeight = level;
                    level = 0;
                }

                mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                drawTexturedModalRect(getGuiLeft() + 150, getGuiTop() + 10 - textureHeight - verticalOffset + 60, icon, 18, textureHeight);
                verticalOffset = verticalOffset + 16;
            }

            GlStateManager.enableBlend();
            this.mc.renderEngine.bindTexture(texture);
            this.drawTexturedModalRect(getGuiLeft() + 150, getGuiTop() + 10, 176, 0, 18, 60);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        // Draw energy tooltips
        if(isPointInRegion(8, 16, 18, 60, mouseX, mouseY)) {
            int energyStored = getContainer().getTile().getEnergyStored();
            int energyMax = getContainer().getTile().getMaxEnergyStored();
            if (energyMax > 0) {
                drawTooltip(Lists.newArrayList(
                        String.format("%,d", energyStored) + " / " + String.format("%,d", energyMax)
                                + " " + L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT)
                ), mouseX - getGuiLeft(), mouseY - getGuiTop());
            }
        }

        // Draw tank tooltips
        if(isPointInRegion(150, 10, 18, 60, mouseX, mouseY)) {
            SingleUseTank tank = getContainer().getTile().getTank();
            FluidStack fluidStack = tank.getFluid();
            if (fluidStack != null) {
                String fluidName = fluidStack.getLocalizedName();
                drawTooltip(Lists.newArrayList(
                        fluidName,
                        DamageIndicatedItemComponent.getInfo(fluidStack, tank.getFluidAmount(), tank.getCapacity())
                ), mouseX - getGuiLeft(), mouseY - getGuiTop());
            }
        }

        // Draw fluid auto-eject toggle
        if(isPointInRegion(150, 70, 18, 10, mouseX, mouseY)) {
            drawTooltip(Lists.newArrayList(
                    L10NHelpers.localize(L10NValues.GUI_MECHANICAL_SQUEEZER_TOGGLEFLUIDAUTOEJECT,
                            TextFormatting.AQUA + L10NHelpers.localize(getContainer().getTile().isAutoEjectFluids() ?
                                    L10NValues.GENERAL_TRUE : L10NValues.GENERAL_FALSE)),
                    L10NHelpers.localize(L10NValues.GUI_MECHANICAL_SQUEEZER_TOGGLEFLUIDAUTOEJECT + ".info")
            ), mouseX - getGuiLeft(), mouseY - getGuiTop());
        }
    }
}
