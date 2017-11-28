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
        GuiHelpers.renderProgressBar(this, getGuiLeft() + 84, getGuiTop() + 31, 11, 28,
                176, 120, GuiHelpers.ProgressDirection.UP,
                getContainer().getLastProgress(), getContainer().getLastMaxProgress());

        // Render energy level
        GuiHelpers.renderProgressBar(this, getGuiLeft() + 8, getGuiTop() + 16, 18, 60,
                176, 60, GuiHelpers.ProgressDirection.UP,
                getContainer().getTile().getEnergyStored(), getContainer().getTile().getMaxEnergyStored());

        // Render input fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, getContainer().getTile().getTankInput().getFluid(),
                getContainer().getTile().getTankInput().getCapacity(), getGuiLeft() + 28, getGuiTop() + 16,
                18, 60, texture, 176, 0);

        // Render output fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, getContainer().getTile().getTankOutput().getFluid(),
                getContainer().getTile().getTankOutput().getCapacity(), getGuiLeft() + 150, getGuiTop() + 16,
                18, 60, texture, 176, 0);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        drawEnergyBarTooltip(8, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(getContainer().getTile().getTankInput(), 28, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(getContainer().getTile().getTankOutput(), 150, 16, 18, 60, mouseX, mouseY);
    }
}
