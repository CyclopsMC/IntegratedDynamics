package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.client.gui.component.input.GuiNumberField;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.integrateddynamics.block.BlockDelayConfig;
import org.cyclops.integrateddynamics.core.client.gui.GuiActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerDelay;
import org.cyclops.integrateddynamics.tileentity.TileDelay;

import java.io.IOException;

/**
 * Gui for the delay.
 * @author rubensworks
 */
public class GuiDelay extends GuiActiveVariableBase<ContainerDelay, TileDelay> {

    private static final int ERROR_X = 110;
    private static final int ERROR_Y = 26;

    private GuiNumberField numberFieldUpdateInterval = null;
    private GuiNumberField numberFieldCapacity = null;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public GuiDelay(InventoryPlayer inventory, TileDelay tile) {
        super(new ContainerDelay(inventory, tile));
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
    public void initGui() {
        super.initGui();

        numberFieldUpdateInterval = new GuiNumberField(0, Minecraft.getMinecraft().fontRenderer, guiLeft + 98, guiTop + 102, 73, 14, true, true);
        numberFieldUpdateInterval.setPositiveOnly(true);
        numberFieldUpdateInterval.setMaxStringLength(64);
        numberFieldUpdateInterval.setMaxStringLength(15);
        numberFieldUpdateInterval.setVisible(true);
        numberFieldUpdateInterval.setTextColor(16777215);
        numberFieldUpdateInterval.setCanLoseFocus(true);

        numberFieldCapacity = new GuiNumberField(0, Minecraft.getMinecraft().fontRenderer, guiLeft + 98, guiTop + 126, 73, 14, true, true);
        numberFieldCapacity.setMinValue(1);
        numberFieldCapacity.setMaxValue(BlockDelayConfig.maxHistoryCapacity);
        numberFieldCapacity.setMaxStringLength(64);
        numberFieldCapacity.setMaxStringLength(15);
        numberFieldCapacity.setVisible(true);
        numberFieldCapacity.setTextColor(16777215);
        numberFieldCapacity.setCanLoseFocus(true);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.checkHotbarKeys(keyCode)) {
            if (!this.numberFieldUpdateInterval.textboxKeyTyped(typedChar, keyCode)
                    && !this.numberFieldCapacity.textboxKeyTyped(typedChar, keyCode)) {
                super.keyTyped(typedChar, keyCode);
            } else {
                onValueChanged();
            }
        }
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
        ValueNotifierHelpers.setValue(getContainer(), getContainer().getLastUpdateValueId(), updateInterval);
        ValueNotifierHelpers.setValue(getContainer(), getContainer().getLastCapacityValueId(), capacity);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.numberFieldUpdateInterval.mouseClicked(mouseX, mouseY, mouseButton);
        this.numberFieldCapacity.mouseClicked(mouseX, mouseY, mouseButton);
        onValueChanged();
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        numberFieldUpdateInterval.drawTextBox(Minecraft.getMinecraft(), mouseX - guiLeft, mouseY - guiTop);
        numberFieldCapacity.drawTextBox(Minecraft.getMinecraft(), mouseX - guiLeft, mouseY - guiTop);
        fontRenderer.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.update_interval"), guiLeft + 8, guiTop + 104, Helpers.RGBToInt(0, 0, 0));
        fontRenderer.drawString(L10NHelpers.localize("gui.integrateddynamics.delay.capacity"), guiLeft + 8, guiTop + 128, Helpers.RGBToInt(0, 0, 0));
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        if (valueId == getContainer().getLastUpdateValueId()) {
            numberFieldUpdateInterval.setText(Integer.toString(getContainer().getLastUpdateValue()));
        }
        if (valueId == ((ContainerDelay) getContainer()).getLastCapacityValueId()) {
            numberFieldCapacity.setText(Integer.toString(((ContainerDelay) getContainer()).getLastCapacityValue()));
        }
    }
}
