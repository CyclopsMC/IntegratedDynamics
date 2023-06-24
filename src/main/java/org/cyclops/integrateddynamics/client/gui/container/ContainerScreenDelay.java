package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetNumberField;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockDelayConfig;
import org.cyclops.integrateddynamics.core.client.gui.ContainerScreenActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerDelay;

/**
 * Gui for the delay.
 * @author rubensworks
 */
public class ContainerScreenDelay extends ContainerScreenActiveVariableBase<ContainerDelay> {

    private static final int ERROR_X = 110;
    private static final int ERROR_Y = 26;

    private WidgetNumberField numberFieldUpdateInterval = null;
    private WidgetNumberField numberFieldCapacity = null;

    public ContainerScreenDelay(ContainerDelay container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/delay.png");
    }

    @Override
    protected int getBaseYSize() {
        return 227;
    }

    @Override
    protected int getErrorX() {
        return ERROR_X;
    }

    @Override
    protected int getErrorY() {
        return ERROR_Y;
    }

    @Override
    public void init() {
        super.init();

        numberFieldUpdateInterval = new WidgetNumberField(font, leftPos + 98, topPos + 102, 73, 14, true,
                Component.translatable("gui.integrateddynamics.partsettings.update_interval"), true);
        numberFieldUpdateInterval.setPositiveOnly(true);
        numberFieldUpdateInterval.setMaxLength(64);
        numberFieldUpdateInterval.setMaxLength(15);
        numberFieldUpdateInterval.setVisible(true);
        numberFieldUpdateInterval.setTextColor(16777215);
        numberFieldUpdateInterval.setCanLoseFocus(true);

        numberFieldCapacity = new WidgetNumberField(font, leftPos + 98, topPos + 126, 73, 14, true,
                Component.translatable("gui.integrateddynamics.delay.capacity"), true);
        numberFieldCapacity.setMinValue(1);
        numberFieldCapacity.setMaxValue(BlockDelayConfig.maxHistoryCapacity);
        numberFieldCapacity.setMaxLength(64);
        numberFieldCapacity.setMaxLength(15);
        numberFieldCapacity.setVisible(true);
        numberFieldCapacity.setTextColor(16777215);
        numberFieldCapacity.setCanLoseFocus(true);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!this.numberFieldUpdateInterval.charTyped(typedChar, keyCode)
                && !this.numberFieldCapacity.charTyped(typedChar, keyCode)) {
            return super.charTyped(typedChar, keyCode);
        } else {
            onValueChanged();
        }

        return true;
    }

    protected void onValueChanged() {
        int updateInterval = 1;
        int capacity = 5;
        try {
            updateInterval = numberFieldUpdateInterval.getInt();
        } catch (NumberFormatException e) {}
        try {
            capacity = numberFieldCapacity.getInt();
        } catch (NumberFormatException e) {}
        ValueNotifierHelpers.setValue(getMenu(), getMenu().getLastUpdateValueId(), updateInterval);
        ValueNotifierHelpers.setValue(getMenu(), getMenu().getLastCapacityValueId(), capacity);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.numberFieldUpdateInterval.mouseClicked(mouseX, mouseY, mouseButton)) {
            onValueChanged();
            return true;
        }
        if (this.numberFieldCapacity.mouseClicked(mouseX, mouseY, mouseButton)) {
            onValueChanged();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        numberFieldUpdateInterval.render(guiGraphics, mouseX - leftPos, mouseY - topPos, partialTicks);
        numberFieldCapacity.render(guiGraphics, mouseX - leftPos, mouseY - topPos, partialTicks);
        // MCP: drawString
        font.drawInBatch(Component.translatable("gui.integrateddynamics.partsettings.update_interval"), leftPos + 8, topPos + 104, Helpers.RGBToInt(0, 0, 0), false,
                guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        font.drawInBatch(Component.translatable("gui.integrateddynamics.delay.capacity"), leftPos + 8, topPos + 128, Helpers.RGBToInt(0, 0, 0), false,
                guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        this.font.drawInBatch(this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752, false,
                guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        displayErrors.drawForeground(guiGraphics.pose(), getMenu().getReadErrors(), getErrorX(), getErrorY(), mouseX, mouseY, this, this.leftPos, this.topPos);
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        if (valueId == getMenu().getLastUpdateValueId()) {
            numberFieldUpdateInterval.setValue(Integer.toString(getMenu().getLastUpdateValue()));
        }
        if (valueId == ((ContainerDelay) getMenu()).getLastCapacityValueId()) {
            numberFieldCapacity.setValue(Integer.toString(((ContainerDelay) getMenu()).getLastCapacityValue()));
        }
    }
}
