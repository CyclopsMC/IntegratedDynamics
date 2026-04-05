package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.cyclopscore.helper.IGuiHelpers;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
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
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/mechanical_squeezer.png");
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(buttonToggleFluidEject = new ButtonImage(getGuiLeftTotal() + 149, getGuiTopTotal() + 71,
                Component.translatable("gui.integrateddynamics.mechanical_squeezer.fluidautoeject"),
                createServerPressable(ContainerMechanicalSqueezer.BUTTON_TOGGLE_FLUID_EJECT, (button) -> {}),imageArrowDownDisabled));
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);

        // Update the image in the fluid eject toggle button
        buttonToggleFluidEject.setImage(getMenu().isAutoEjectFluids()
                ? imageArrowDownEnabled : imageArrowDownDisabled);

        // Render progress
        IModHelpers.get().getGuiHelpers().renderProgressBar(guiGraphics, getGuiTexture(), getGuiLeftTotal() + 73, getGuiTopTotal() + 36, 12, 18,
                176, 120, IGuiHelpers.ProgressDirection.DOWN,
                getMenu().getProgress(), getMenu().getMaxProgress());

        // Render energy level
        IModHelpers.get().getGuiHelpers().renderProgressBar(guiGraphics, getGuiTexture(), getGuiLeftTotal() + 8, getGuiTopTotal() + 16, 18, 60,
                176, 60, IGuiHelpers.ProgressDirection.UP,
                getMenu().getEnergy(), getMenu().getMaxEnergy());

        // Render fluid tank
        IModHelpersNeoForge.get().getGuiHelpers().renderOverlayedFluidTank(guiGraphics, getMenu().getFluidStack(),
                getMenu().getFluidCapacity(), getGuiLeftTotal() + 150, getGuiTopTotal() + 10,
                18, 60, texture, 176, 0);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);

        drawEnergyBarTooltip(guiGraphics, 8, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(guiGraphics, getMenu().getFluidStack(), getMenu().getFluidCapacity(), 150, 10, 18, 60, mouseX, mouseY);

        // Draw fluid auto-eject toggle
        IModHelpers.get().getGuiHelpers().renderTooltip(this, guiGraphics, 150, 70, 18, 10, mouseX, mouseY, () -> Lists.newArrayList(
                Component.translatable(L10NValues.GUI_MECHANICAL_SQUEEZER_TOGGLEFLUIDAUTOEJECT,
                        ChatFormatting.AQUA + IModHelpers.get().getL10NHelpers().localize(getMenu().isAutoEjectFluids() ?
                                L10NValues.GENERAL_TRUE : L10NValues.GENERAL_FALSE)),
                Component.translatable(L10NValues.GUI_MECHANICAL_SQUEEZER_TOGGLEFLUIDAUTOEJECT + ".info")));
    }
}
