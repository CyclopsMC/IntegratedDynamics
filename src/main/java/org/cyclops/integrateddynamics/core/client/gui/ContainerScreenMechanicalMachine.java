package org.cyclops.integrateddynamics.core.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.item.DamageIndicatedItemComponent;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMechanicalMachine;

import java.util.Optional;

/**
 * Base class for mechanical machine guis.
 * @author rubensworks
 */
public abstract class ContainerScreenMechanicalMachine<C extends ContainerMechanicalMachine<?>> extends ContainerScreenExtended<C> {

    public ContainerScreenMechanicalMachine(C container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }

    public void drawEnergyBarTooltip(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        IModHelpers.get().getGuiHelpers().renderTooltipOptional(this, guiGraphics, 8, 16, 18, 60, mouseX, mouseY, () -> {
            int energyStored = getMenu().getEnergy();
            int energyMax = getMenu().getMaxEnergy();
            if (energyMax > 0) {
                return Optional.of(Lists.newArrayList(
                        Component.translatable("general.integrateddynamics.energy"),
                        Helpers.getLocalizedEnergyLevel(energyStored, energyMax)));
            }
            return Optional.empty();
        });
    }

    public void drawFluidTankTooltip(GuiGraphicsExtractor guiGraphics, FluidStack fluidStack, int fluidCapacity, int x, int y, int width, int height, int mouseX, int mouseY) {
        IModHelpers.get().getGuiHelpers().renderTooltipOptional(this, guiGraphics, x, y, width, height, mouseX, mouseY, () -> {
            if (fluidStack != null && !fluidStack.isEmpty()) {
                Component fluidName = fluidStack.getHoverName();
                return Optional.of(Lists.newArrayList(fluidName,
                        DamageIndicatedItemComponent.getInfo(fluidStack, fluidStack.getAmount(), fluidCapacity)));
            }
            return Optional.empty();
        });
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        guiGraphics.text(font, this.title, this.titleLabelX, this.titleLabelY, ARGB.opaque(4210752), false);
    }
}
