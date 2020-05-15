package org.cyclops.integrateddynamics.core.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.item.DamageIndicatedItemComponent;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMechanicalMachine;

import java.util.Optional;

/**
 * Base class for mechanical machine guis.
 * @author rubensworks
 */
public abstract class ContainerScreenMechanicalMachine<C extends ContainerMechanicalMachine<?>> extends ContainerScreenExtended<C> {

    public ContainerScreenMechanicalMachine(C container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
    }

    public void drawEnergyBarTooltip(int x, int y, int width, int height, int mouseX, int mouseY) {
        GuiHelpers.renderTooltipOptional(this, 8, 16, 18, 60, mouseX, mouseY, () -> {
            int energyStored = getContainer().getEnergy();
            int energyMax = getContainer().getMaxEnergy();
            if (energyMax > 0) {
                return Optional.of(Lists.newArrayList(
                        new TranslationTextComponent("general.integrateddynamics.energy"),
                        Helpers.getLocalizedEnergyLevel(energyStored, energyMax)));
            }
            return Optional.empty();
        });
    }

    public void drawFluidTankTooltip(FluidStack fluidStack, int fluidCapacity, int x, int y, int width, int height, int mouseX, int mouseY) {
        GuiHelpers.renderTooltipOptional(this, x, y, width, height, mouseX, mouseY, () -> {
            if (fluidStack != null) {
                ITextComponent fluidName = fluidStack.getDisplayName();
                return Optional.of(Lists.newArrayList(fluidName,
                        DamageIndicatedItemComponent.getInfo(fluidStack, fluidStack.getAmount(), fluidCapacity)));
            }
            return Optional.empty();
        });
    }
}
