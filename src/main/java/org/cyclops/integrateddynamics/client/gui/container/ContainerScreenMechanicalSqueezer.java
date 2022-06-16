package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.gui.ContainerScreenMechanicalMachine;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalSqueezer;

/**
 * Gui for the mechanical squeezer.
 * @author rubensworks
 */
public class ContainerScreenMechanicalSqueezer extends ContainerScreenMechanicalMachine<ContainerMechanicalSqueezer> {

    private final IImage imageArrowDownEnabled;
    private final IImage imageArrowDownDisabled;
    private ButtonImage buttonToggleFluidEject;

    public ContainerScreenMechanicalSqueezer(ContainerMechanicalSqueezer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageArrowDownEnabled = new Image(texture, 176, 138, 20, 10);
        imageArrowDownDisabled = new Image(texture, 176, 148, 20, 10);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/mechanical_squeezer.png");
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(buttonToggleFluidEject = new ButtonImage(getGuiLeftTotal() + 149, getGuiTopTotal() + 71,
                Component.translatable("gui.integrateddynamics.mechanical_squeezer.fluidautoeject"),
                createServerPressable(ContainerMechanicalSqueezer.BUTTON_TOGGLE_FLUID_EJECT, (button) -> {}),imageArrowDownDisabled));
    }

    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        // Update the image in the fluid eject toggle button
        buttonToggleFluidEject.setImage(getMenu().isAutoEjectFluids()
                ? imageArrowDownEnabled : imageArrowDownDisabled);

        // Render progress
        GuiHelpers.renderProgressBar(this, matrixStack, getGuiLeftTotal() + 73, getGuiTopTotal() + 36, 12, 18,
                176, 120, GuiHelpers.ProgressDirection.DOWN,
                getMenu().getProgress(), getMenu().getMaxProgress());

        // Render energy level
        GuiHelpers.renderProgressBar(this, matrixStack, getGuiLeftTotal() + 8, getGuiTopTotal() + 16, 18, 60,
                176, 60, GuiHelpers.ProgressDirection.UP,
                getMenu().getEnergy(), getMenu().getMaxEnergy());

        // Render fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, matrixStack, getMenu().getFluidStack(),
                getMenu().getFluidCapacity(), getGuiLeftTotal() + 150, getGuiTopTotal() + 10,
                18, 60, texture, 176, 0);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);

        drawEnergyBarTooltip(matrixStack, 8, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(matrixStack, getMenu().getFluidStack(), getMenu().getFluidCapacity(), 150, 10, 18, 60, mouseX, mouseY);

        // Draw fluid auto-eject toggle
        GuiHelpers.renderTooltip(this, matrixStack, 150, 70, 18, 10, mouseX, mouseY, () -> Lists.newArrayList(
                Component.translatable(L10NValues.GUI_MECHANICAL_SQUEEZER_TOGGLEFLUIDAUTOEJECT,
                        ChatFormatting.AQUA + L10NHelpers.localize(getMenu().isAutoEjectFluids() ?
                                L10NValues.GENERAL_TRUE : L10NValues.GENERAL_FALSE)),
                Component.translatable(L10NValues.GUI_MECHANICAL_SQUEEZER_TOGGLEFLUIDAUTOEJECT + ".info")));
    }
}
