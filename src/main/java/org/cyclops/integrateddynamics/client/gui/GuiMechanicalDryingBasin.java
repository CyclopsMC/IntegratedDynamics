package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integrateddynamics.core.client.gui.GuiMechanicalMachine;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalDryingBasin;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalDryingBasin;

/**
 * Gui for the mechanical squeezer.
 * @author rubensworks
 */
public class GuiMechanicalDryingBasin extends GuiMechanicalMachine<ContainerMechanicalDryingBasin> {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public GuiMechanicalDryingBasin(InventoryPlayer inventory, TileMechanicalDryingBasin tile) {
        super(new ContainerMechanicalDryingBasin(inventory, tile));
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        // Render progress
        GuiHelpers.renderProgressBar(this, getGuiLeftTotal() + 84, getGuiTopTotal() + 31, 11, 28,
                176, 120, GuiHelpers.ProgressDirection.UP,
                getContainer().getProgress(), getContainer().getMaxProgress());

        // Render energy level
        GuiHelpers.renderProgressBar(this, getGuiLeftTotal() + 8, getGuiTopTotal() + 16, 18, 60,
                176, 60, GuiHelpers.ProgressDirection.UP,
                getContainer().getEnergy(), getContainer().getMaxEnergy());

        // Render input fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, getContainer().getInputFluidStack(),
                getContainer().getInputFluidCapacity(), getGuiLeftTotal() + 28, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);

        // Render output fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, getContainer().getOutputFluidStack(),
                getContainer().getOutputFluidCapacity(), getGuiLeftTotal() + 150, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        drawEnergyBarTooltip(8, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(getContainer().getInputFluidStack(), getContainer().getInputFluidCapacity(), 28, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(getContainer().getOutputFluidStack(), getContainer().getOutputFluidCapacity(), 150, 16, 18, 60, mouseX, mouseY);
    }
}
