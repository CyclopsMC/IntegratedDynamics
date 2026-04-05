package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.helper.IGuiHelpers;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
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
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/mechanical_drying_basin.png");
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);

        // Render progress
        IModHelpers.get().getGuiHelpers().renderProgressBar(guiGraphics, getGuiTexture(), getGuiLeftTotal() + 84, getGuiTopTotal() + 31, 11, 28,
                176, 120, IGuiHelpers.ProgressDirection.UP,
                getMenu().getProgress(), getMenu().getMaxProgress());

        // Render energy level
        IModHelpers.get().getGuiHelpers().renderProgressBar(guiGraphics, getGuiTexture(), getGuiLeftTotal() + 8, getGuiTopTotal() + 16, 18, 60,
                176, 60, IGuiHelpers.ProgressDirection.UP,
                getMenu().getEnergy(), getMenu().getMaxEnergy());

        // Render input fluid tank
        IModHelpersNeoForge.get().getGuiHelpers().renderOverlayedFluidTank(guiGraphics, getMenu().getInputFluidStack(),
                getMenu().getInputFluidCapacity(), getGuiLeftTotal() + 28, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);

        // Render output fluid tank
        IModHelpersNeoForge.get().getGuiHelpers().renderOverlayedFluidTank(guiGraphics, getMenu().getOutputFluidStack(),
                getMenu().getOutputFluidCapacity(), getGuiLeftTotal() + 150, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);

        drawEnergyBarTooltip(guiGraphics, 8, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(guiGraphics, getMenu().getInputFluidStack(), getMenu().getInputFluidCapacity(), 28, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(guiGraphics, getMenu().getOutputFluidStack(), getMenu().getOutputFluidCapacity(), 150, 16, 18, 60, mouseX, mouseY);
    }
}
