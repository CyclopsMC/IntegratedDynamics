package org.cyclops.integrateddynamics.core.client.gui;

import com.google.common.collect.Lists;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.item.DamageIndicatedItemComponent;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
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
            int energyStored = getContainer().getTile().getEnergyStored();
            int energyMax = getContainer().getTile().getMaxEnergyStored();
            if (energyMax > 0) {
                return Optional.of(Lists.newArrayList(
                        String.format("%,d", energyStored) + " / " + String.format("%,d", energyMax)
                                + " " + L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT)));
            }
            return Optional.empty();
        });
    }

    public void drawFluidTankTooltip(IFluidTank tank, int x, int y, int width, int height, int mouseX, int mouseY) {
        GuiHelpers.renderTooltipOptional(this, x, y, width, height, mouseX, mouseY, () -> {
            FluidStack fluidStack = tank.getFluid();
            if (fluidStack != null) {
                String fluidName = fluidStack.getLocalizedName();
                return Optional.of(Lists.newArrayList(fluidName,
                        DamageIndicatedItemComponent.getInfo(fluidStack, tank.getFluidAmount(), tank.getCapacity())));
            }
            return Optional.empty();
        });
    }
}
