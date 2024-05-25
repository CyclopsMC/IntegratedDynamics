package org.cyclops.integrateddynamics.core.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
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

    public ContainerScreenMechanicalMachine(C container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }

    public void drawEnergyBarTooltip(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY) {
        GuiHelpers.renderTooltipOptional(this, poseStack, 8, 16, 18, 60, mouseX, mouseY, () -> {
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

    public void drawFluidTankTooltip(PoseStack poseStack, FluidStack fluidStack, int fluidCapacity, int x, int y, int width, int height, int mouseX, int mouseY) {
        GuiHelpers.renderTooltipOptional(this, poseStack, x, y, width, height, mouseX, mouseY, () -> {
            if (fluidStack != null && !fluidStack.isEmpty()) {
                Component fluidName = fluidStack.getDisplayName();
                return Optional.of(Lists.newArrayList(fluidName,
                        DamageIndicatedItemComponent.getInfo(fluidStack, fluidStack.getAmount(), fluidCapacity)));
            }
            return Optional.empty();
        });
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        this.font.drawInBatch(this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752, false,
                guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }
}
