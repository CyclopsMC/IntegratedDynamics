package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetNumberField;
import org.cyclops.cyclopscore.helper.IModHelpers;
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
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/delay.png");
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
        numberFieldUpdateInterval.setTextColor(ARGB.opaque(16777215));
        numberFieldUpdateInterval.setCanLoseFocus(true);
        addWidget(numberFieldUpdateInterval);

        numberFieldCapacity = new WidgetNumberField(font, leftPos + 98, topPos + 126, 73, 14, true,
                Component.translatable("gui.integrateddynamics.delay.capacity"), true);
        numberFieldCapacity.setMinValue(1);
        numberFieldCapacity.setMaxValue(BlockDelayConfig.maxHistoryCapacity);
        numberFieldCapacity.setMaxLength(64);
        numberFieldCapacity.setMaxLength(15);
        numberFieldCapacity.setVisible(true);
        numberFieldCapacity.setTextColor(ARGB.opaque(16777215));
        numberFieldCapacity.setCanLoseFocus(true);
        addWidget(numberFieldCapacity);
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if (!this.numberFieldUpdateInterval.charTyped(evt)
                && !this.numberFieldCapacity.charTyped(evt)) {
            return super.charTyped(evt);
        } else {
            onValueChanged();
        }

        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (!this.numberFieldUpdateInterval.keyPressed(evt)
                && !this.numberFieldCapacity.keyPressed(evt)) {
            return super.keyPressed(evt);
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
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        boolean clicked = false;
        if (this.numberFieldUpdateInterval.mouseClicked(evt, isDoubleClick)) {
            onValueChanged();
            clicked = true;
        }
        if (this.numberFieldCapacity.mouseClicked(evt, isDoubleClick)) {
            onValueChanged();
            clicked = true;
        }
        return clicked || super.mouseClicked(evt, isDoubleClick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        numberFieldUpdateInterval.extractRenderState(guiGraphics, mouseX - leftPos, mouseY - topPos, partialTicks);
        numberFieldCapacity.extractRenderState(guiGraphics, mouseX - leftPos, mouseY - topPos, partialTicks);
        // MCP: drawString
        guiGraphics.text(font, Component.translatable("gui.integrateddynamics.partsettings.update_interval"), leftPos + 8, topPos + 104, IModHelpers.get().getBaseHelpers().RGBAToInt(0, 0, 0, 255), false);
        guiGraphics.text(font, Component.translatable("gui.integrateddynamics.delay.capacity"), leftPos + 8, topPos + 128, IModHelpers.get().getBaseHelpers().RGBAToInt(0, 0, 0, 255), false);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        guiGraphics.text(font, this.title, this.titleLabelX, this.titleLabelY, ARGB.opaque(4210752), false);
        displayErrors.drawForeground(guiGraphics, getMenu().getReadErrors(), getErrorX(), getErrorY(), mouseX, mouseY, this, this.leftPos, this.topPos);
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
