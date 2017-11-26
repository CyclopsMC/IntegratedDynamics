package org.cyclops.integrateddynamics.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.client.gui.GuiMechanicalMachine;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerMechanicalSqueezer;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalSqueezer;

/**
 * Gui for the mechanical squeezer.
 * @author rubensworks
 */
public class GuiMechanicalSqueezer extends GuiMechanicalMachine<ContainerMechanicalSqueezer> {

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
        GuiHelpers.renderProgressBar(this, getGuiLeft() + 73, getGuiTop() + 36, 12, 18,
                176, 120, GuiHelpers.ProgressDirection.DOWN,
                getContainer().getLastProgress(), getContainer().getLastMaxProgress());

        // Render energy level
        GuiHelpers.renderProgressBar(this, getGuiLeft() + 8, getGuiTop() + 16, 18, 60,
                176, 60, GuiHelpers.ProgressDirection.UP,
                getContainer().getTile().getEnergyStored(), getContainer().getTile().getMaxEnergyStored());

        // Render fluid tank
        GuiHelpers.renderOverlayedFluidTank(this, getContainer().getTile().getTank().getFluid(),
                getContainer().getTile().getTank().getCapacity(), getGuiLeft() + 150, getGuiTop() + 10,
                18, 60, texture, 176, 0);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        drawEnergyBarTooltip(8, 16, 18, 60, mouseX, mouseY);
        drawFluidTankTooltip(getContainer().getTile().getTank(), 150, 10, 18, 60, mouseX, mouseY);

        // Draw fluid auto-eject toggle
        GuiHelpers.renderTooltip(this, 150, 70, 18, 10, mouseX, mouseY, () -> Lists.newArrayList(
                L10NHelpers.localize(L10NValues.GUI_MECHANICAL_SQUEEZER_TOGGLEFLUIDAUTOEJECT,
                        TextFormatting.AQUA + L10NHelpers.localize(getContainer().getTile().isAutoEjectFluids() ?
                                L10NValues.GENERAL_TRUE : L10NValues.GENERAL_FALSE)),
                L10NHelpers.localize(L10NValues.GUI_MECHANICAL_SQUEEZER_TOGGLEFLUIDAUTOEJECT + ".info")));
    }
}
