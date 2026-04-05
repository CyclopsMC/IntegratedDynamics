package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.IRenderPatternValueTypeTooltip;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeRecipeLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeRecipeValueChangedPacket;

import java.util.List;

/**
 * @author rubensworks
 */
public class ValueTypeRecipeLPElementRecipeSubGui extends RenderPattern<ValueTypeRecipeLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
        implements IRenderPatternValueTypeTooltip {

    private boolean renderTooltip = true;
    private WidgetTextFieldExtended inputFluidAmountBox = null;
    private WidgetTextFieldExtended inputEnergyBox = null;
    private WidgetTextFieldExtended outputFluidAmountBox = null;
    private WidgetTextFieldExtended outputEnergyBox = null;

    public boolean isRenderTooltip() {
        return renderTooltip;
    }

    public void setRenderTooltip(boolean renderTooltip) {
        this.renderTooltip = renderTooltip;
    }

    public WidgetTextFieldExtended getInputFluidAmountBox() {
        return inputFluidAmountBox;
    }

    public WidgetTextFieldExtended getInputEnergyBox() {
        return inputEnergyBox;
    }

    public WidgetTextFieldExtended getOutputFluidAmountBox() {
        return outputFluidAmountBox;
    }

    public WidgetTextFieldExtended getOutputEnergyBox() {
        return outputEnergyBox;
    }

    public ValueTypeRecipeLPElementRecipeSubGui(ValueTypeRecipeLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                                ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    protected static WidgetTextFieldExtended makeTextBox(int componentId, int x, int y, String text) {
        Font fontRenderer = Minecraft.getInstance().font;
        int searchWidth = 35;

        WidgetTextFieldExtended box = new WidgetTextFieldExtended(fontRenderer, x, y,
                searchWidth, fontRenderer.lineHeight + 3, Component.translatable("gui.cyclopscore.search"), true);
        box.setMaxLength(10);
        box.setBordered(false);
        box.setVisible(true);
        box.setTextColor(ARGB.opaque(16777215));
        box.setCanLoseFocus(true);
        box.setValue(text);
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
    public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

        // Output type tooltip
        this.drawTooltipForeground(gui, guiGraphics, container, guiLeft, guiTop, mouseX, mouseY, element.getValueType());

        // Render the info tooltip when hovering the input item slots
        for (int slotId = 0; slotId < this.container.slots.size(); ++slotId) {
            Slot slot = this.container.slots.get(slotId);
            if (slotId >= ValueTypeRecipeLPElement.SLOT_OFFSET && slotId < 9 + ValueTypeRecipeLPElement.SLOT_OFFSET) {
                int slotX = slot.x;
                int slotY = slot.y;

                // Draw tooltips
                if (gui.isHovering(slotX - 1, slotY - 1, 18, 18, mouseX, mouseY)) {
                    List<Component> tooltipLines = slot.getItem().isEmpty() ?
                            Lists.newArrayList() :
                            slot.getItem().getTooltipLines(
                                    Item.TooltipContext.of(getContainer().getPlayerIInventory().player.level()),
                                    getContainer().getPlayerIInventory().player,
                                    TooltipFlag.NORMAL
                            );
                    tooltipLines.add(Component.translatable("valuetype.integrateddynamics.ingredients.slot.info")
                            .withStyle(ChatFormatting.ITALIC));
                    gui.drawTooltip(tooltipLines, guiGraphics, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

        // Draw crafting arrow
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SubGuiBox.TEXTURE, guiLeft + getX() + 66, guiTop + getY() + 21, 0, 38, 22, 15, 256, 256);

        inputFluidAmountBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.text(fontRenderer, IModHelpers.get().getL10NHelpers().localize(L10NValues.GENERAL_ENERGY_UNIT) + ":", guiLeft + getX() + 2, guiTop + getY() + 78, ARGB.opaque(0), false);
        inputEnergyBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        outputFluidAmountBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.text(fontRenderer, IModHelpers.get().getL10NHelpers().localize(L10NValues.GENERAL_ENERGY_UNIT) + ":", guiLeft + getX() + 84, guiTop + getY() + 78, ARGB.opaque(0), false);
        outputEnergyBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if (inputFluidAmountBox.charTyped(evt)) {
            element.setInputFluidAmount(inputFluidAmountBox.getValue());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getInputFluidAmount(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.INPUT_FLUID));
            return true;
        }
        if (inputEnergyBox.charTyped(evt)) {
            element.setInputEnergy(inputEnergyBox.getValue());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getInputEnergy(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.INPUT_ENERGY));
            return true;
        }
        if (outputFluidAmountBox.charTyped(evt)) {
            element.setOutputFluidAmount(outputFluidAmountBox.getValue());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getOutputFluidAmount(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.OUTPUT_FLUID));
            return true;
        }
        if (outputEnergyBox.charTyped(evt)) {
            element.setOutputEnergy(outputEnergyBox.getValue());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getOutputEnergy(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.OUTPUT_ENERGY));
            return true;
        }
        return super.charTyped(evt);
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (inputFluidAmountBox.keyPressed(evt)) {
            element.setInputFluidAmount(inputFluidAmountBox.getValue());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getInputFluidAmount(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.INPUT_FLUID));
            return true;
        }
        if (inputEnergyBox.keyPressed(evt)) {
            element.setInputEnergy(inputEnergyBox.getValue());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getInputEnergy(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.INPUT_ENERGY));
            return true;
        }
        if (outputFluidAmountBox.keyPressed(evt)) {
            element.setOutputFluidAmount(outputFluidAmountBox.getValue());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getOutputFluidAmount(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.OUTPUT_FLUID));
            return true;
        }
        if (outputEnergyBox.keyPressed(evt)) {
            element.setOutputEnergy(outputEnergyBox.getValue());
            container.onDirty();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeRecipeValueChangedPacket(element.getOutputEnergy(),
                            LogicProgrammerValueTypeRecipeValueChangedPacket.Type.OUTPUT_ENERGY));
            return true;
        }
        return super.keyPressed(evt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        return inputFluidAmountBox.mouseClicked(evt, isDoubleClick)
                || inputEnergyBox.mouseClicked(evt, isDoubleClick)
                || outputFluidAmountBox.mouseClicked(evt, isDoubleClick)
                || outputEnergyBox.mouseClicked(evt, isDoubleClick)
                || super.mouseClicked(evt, isDoubleClick);
    }
}
