package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeRecipeValueChangedPacket;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
class ValueTypeRecipeLPElementRecipeSubGui extends RenderPattern<ValueTypeRecipeLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
        implements IRenderPatternValueTypeTooltip {

    @Getter
    @Setter
    private boolean renderTooltip = true;
    @Getter
    private WidgetTextFieldExtended inputFluidAmountBox = null;
    @Getter
    private WidgetTextFieldExtended inputEnergyBox = null;
    @Getter
    private WidgetTextFieldExtended outputFluidAmountBox = null;
    @Getter
    private WidgetTextFieldExtended outputEnergyBox = null;

    public ValueTypeRecipeLPElementRecipeSubGui(ValueTypeRecipeLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                                ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    protected static WidgetTextFieldExtended makeTextBox(int componentId, int x, int y, String text) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int searchWidth = 35;

        WidgetTextFieldExtended box = new WidgetTextFieldExtended(fontRenderer, x, y,
                searchWidth, fontRenderer.FONT_HEIGHT + 3, new TranslationTextComponent("gui.cyclopscore.search"), true);
        box.setMaxStringLength(10);
        box.setEnableBackgroundDrawing(false);
        box.setVisible(true);
        box.setTextColor(16777215);
        box.setCanLoseFocus(true);
        box.setText(text);
        box.setWidth(searchWidth);
        return box;
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);

        this.inputFluidAmountBox = makeTextBox(0, guiLeft + getX() + 21, guiTop + getY() + 59, element.getInputFluidAmount());
        this.inputEnergyBox = makeTextBox(1, guiLeft + getX() + 21, guiTop + getY() + 77, element.getInputEnergy());
        this.outputFluidAmountBox = makeTextBox(2, guiLeft + getX() + 101, guiTop + getY() + 59, element.getOutputFluidAmount());
        this.outputEnergyBox = makeTextBox(3, guiLeft + getX() + 101, guiTop + getY() + 77, element.getOutputEnergy());
    }

    @Override
    public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

        // Output type tooltip
        this.drawTooltipForeground(gui, container, guiLeft, guiTop, mouseX, mouseY, element.getValueType());

        // Render the info tooltip when hovering the input item slots
        for (int slotId = 0; slotId < this.container.inventorySlots.size(); ++slotId) {
            Slot slot = this.container.inventorySlots.get(slotId);
            if (slotId >= ValueTypeRecipeLPElement.SLOT_OFFSET && slotId < 9 + ValueTypeRecipeLPElement.SLOT_OFFSET) {
                int slotX = slot.xPos;
                int slotY = slot.yPos;

                // Draw tooltips
                if (gui.isPointInRegion(slotX, slotY, 16, 16, mouseX, mouseY)) {
                    gui.drawTooltip(Lists.newArrayList(
                            new TranslationTextComponent("valuetype.integrateddynamics.ingredients.slot.info")
                                    .mergeStyle(TextFormatting.ITALIC)
                    ), mouseX - guiLeft, mouseY - guiTop - (slot.getStack().isEmpty() ? 0 : 15));
                }
            }
        }
    }

    @Override
    public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

        // Draw crafting arrow
        this.blit(matrixStack, guiLeft + getX() + 66, guiTop + getY() + 21, 0, 38, 22, 15);

        inputFluidAmountBox.render(matrixStack, mouseX, mouseY, partialTicks);
        fontRenderer.drawString(matrixStack, L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT) + ":", guiLeft + getX() + 2, guiTop + getY() + 78, 0);
        inputEnergyBox.render(matrixStack, mouseX, mouseY, partialTicks);
        outputFluidAmountBox.render(matrixStack, mouseX, mouseY, partialTicks);
        fontRenderer.drawString(matrixStack, L10NHelpers.localize(L10NValues.GENERAL_ENERGY_UNIT) + ":", guiLeft + getX() + 84, guiTop + getY() + 78, 0);
        outputEnergyBox.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (inputFluidAmountBox.charTyped(typedChar, keyCode)) {
            element.setInputFluidAmount(inputFluidAmountBox.getText());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getInputFluidAmount(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.INPUT_FLUID));
            return true;
        }
        if (inputEnergyBox.charTyped(typedChar, keyCode)) {
            element.setInputEnergy(inputEnergyBox.getText());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getInputEnergy(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.INPUT_ENERGY));
            return true;
        }
        if (outputFluidAmountBox.charTyped(typedChar, keyCode)) {
            element.setOutputFluidAmount(outputFluidAmountBox.getText());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getOutputFluidAmount(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.OUTPUT_FLUID));
            return true;
        }
        if (outputEnergyBox.charTyped(typedChar, keyCode)) {
            element.setOutputEnergy(outputEnergyBox.getText());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getOutputEnergy(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.OUTPUT_ENERGY));
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return inputFluidAmountBox.mouseClicked(mouseX, mouseY, mouseButton)
                || inputEnergyBox.mouseClicked(mouseX, mouseY, mouseButton)
                || outputFluidAmountBox.mouseClicked(mouseX, mouseY, mouseButton)
                || outputEnergyBox.mouseClicked(mouseX, mouseY, mouseButton)
                || super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
