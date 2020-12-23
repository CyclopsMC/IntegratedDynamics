package org.cyclops.integrateddynamics.client.gui.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.gui.ContainerScreenMechanicalMachine;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalDryingBasin;

/**
 * Gui for the mechanical squeezer.
 * @author rubensworks
 */
public class ContainerScreenMechanicalDryingBasin extends ContainerScreenMechanicalMachine<ContainerMechanicalDryingBasin> {

    public ContainerScreenMechanicalDryingBasin(ContainerMechanicalDryingBasin container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/mechanical_drying_basin.png");
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        // Render progress
        GuiHelpers.renderProgressBar(this, matrixStack, getGuiLeftTotal() + 84, getGuiTopTotal() + 31, 11, 28,
                176, 120, GuiHelpers.ProgressDirection.UP,
                getContainer().getProgress(), getContainer().getMaxProgress());

        // Render energy level
        GuiHelpers.renderProgressBar(this, matrixStack, getGuiLeftTotal() + 8, getGuiTopTotal() + 16, 18, 60,
                176, 60, GuiHelpers.ProgressDirection.UP,
                getContainer().getEnergy(), getContainer().getMaxEnergy());

        // Render input fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, matrixStack, getContainer().getInputFluidStack(),
                getContainer().getInputFluidCapacity(), getGuiLeftTotal() + 28, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);

        // Render output fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, matrixStack, getContainer().getOutputFluidStack(),
                getContainer().getOutputFluidCapacity(), getGuiLeftTotal() + 150, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        drawEnergyBarTooltip(8, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(getContainer().getInputFluidStack(), getContainer().getInputFluidCapacity(), 28, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(getContainer().getOutputFluidStack(), getContainer().getOutputFluidCapacity(), 150, 16, 18, 60, mouseX, mouseY);
    }
}
