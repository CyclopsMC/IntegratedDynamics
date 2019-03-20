package org.cyclops.integrateddynamics.core.client.gui;

import com.google.common.collect.Lists;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.item.DamageIndicatedItemComponent;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMechanicalMachine;

import java.util.Optional;

/**
 * Base class for mechanical machine guis.
 * @author rubensworks
 */
public class GuiMechanicalMachine<C extends ContainerMechanicalMachine<?>> extends GuiContainerConfigurable<C> {


    /**
     * Make a new instance.
     * @param container The container to make the GUI for.
     */
    public GuiMechanicalMachine(C container) {
        super(container);
    }

    public void drawEnergyBarTooltip(int x, int y, int width, int height, int mouseX, int mouseY) {
        GuiHelpers.renderTooltipOptional(this, 8, 16, 18, 60, mouseX, mouseY, () -> {
            int energyStored = getContainer().getEnergy();
            int energyMax = getContainer().getMaxEnergy();
            if (energyMax > 0) {
                return Optional.of(Lists.newArrayList(Helpers.getLocalizedEnergyLevel(energyStored, energyMax)));
            }
            return Optional.empty();
        });
    }

    public void drawFluidTankTooltip(FluidStack fluidStack, int fluidCapacity, int x, int y, int width, int height, int mouseX, int mouseY) {
        GuiHelpers.renderTooltipOptional(this, x, y, width, height, mouseX, mouseY, () -> {
            if (fluidStack != null) {
                String fluidName = fluidStack.getLocalizedName();
                return Optional.of(Lists.newArrayList(fluidName,
                        DamageIndicatedItemComponent.getInfo(fluidStack, fluidStack.amount, fluidCapacity)));
            }
            return Optional.empty();
        });
    }
}
