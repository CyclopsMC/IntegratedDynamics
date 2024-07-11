package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.gui.ContainerScreenMechanicalMachine;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalDryingBasin;

/**
 * Gui for the mechanical squeezer.
 * @author rubensworks
 */
public class ContainerScreenMechanicalDryingBasin extends ContainerScreenMechanicalMachine<ContainerMechanicalDryingBasin> {

    public ContainerScreenMechanicalDryingBasin(ContainerMechanicalDryingBasin container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/mechanical_drying_basin.png");
    }

    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);

        // Render progress
        GuiHelpers.renderProgressBar(guiGraphics, getGuiTexture(), getGuiLeftTotal() + 84, getGuiTopTotal() + 31, 11, 28,
                176, 120, GuiHelpers.ProgressDirection.UP,
                getMenu().getProgress(), getMenu().getMaxProgress());

        // Render energy level
        GuiHelpers.renderProgressBar(guiGraphics, getGuiTexture(), getGuiLeftTotal() + 8, getGuiTopTotal() + 16, 18, 60,
                176, 60, GuiHelpers.ProgressDirection.UP,
                getMenu().getEnergy(), getMenu().getMaxEnergy());

        // Render input fluid tank
        GuiHelpers.renderOverlayedFluidTank(guiGraphics, getMenu().getInputFluidStack(),
                getMenu().getInputFluidCapacity(), getGuiLeftTotal() + 28, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);

        // Render output fluid tank
        GuiHelpers.renderOverlayedFluidTank(guiGraphics, getMenu().getOutputFluidStack(),
                getMenu().getOutputFluidCapacity(), getGuiLeftTotal() + 150, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        drawEnergyBarTooltip(guiGraphics.pose(), 8, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(guiGraphics.pose(), getMenu().getInputFluidStack(), getMenu().getInputFluidCapacity(), 28, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(guiGraphics.pose(), getMenu().getOutputFluidStack(), getMenu().getOutputFluidCapacity(), 150, 16, 18, 60, mouseX, mouseY);
    }
}
