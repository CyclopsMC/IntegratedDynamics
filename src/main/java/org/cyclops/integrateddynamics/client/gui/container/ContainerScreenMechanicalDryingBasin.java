package org.cyclops.integrateddynamics.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
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
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/mechanical_drying_basin.png");
    }

    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        // Render progress
        GuiHelpers.renderProgressBar(this, matrixStack, getGuiLeftTotal() + 84, getGuiTopTotal() + 31, 11, 28,
                176, 120, GuiHelpers.ProgressDirection.UP,
                getMenu().getProgress(), getMenu().getMaxProgress());

        // Render energy level
        GuiHelpers.renderProgressBar(this, matrixStack, getGuiLeftTotal() + 8, getGuiTopTotal() + 16, 18, 60,
                176, 60, GuiHelpers.ProgressDirection.UP,
                getMenu().getEnergy(), getMenu().getMaxEnergy());

        // Render input fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, matrixStack, getMenu().getInputFluidStack(),
                getMenu().getInputFluidCapacity(), getGuiLeftTotal() + 28, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);

        // Render output fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, matrixStack, getMenu().getOutputFluidStack(),
                getMenu().getOutputFluidCapacity(), getGuiLeftTotal() + 150, getGuiTopTotal() + 16,
                18, 60, texture, 176, 0);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);

        drawEnergyBarTooltip(matrixStack, 8, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(matrixStack, getMenu().getInputFluidStack(), getMenu().getInputFluidCapacity(), 28, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(matrixStack, getMenu().getOutputFluidStack(), getMenu().getOutputFluidCapacity(), 150, 16, 18, 60, mouseX, mouseY);
    }
}
