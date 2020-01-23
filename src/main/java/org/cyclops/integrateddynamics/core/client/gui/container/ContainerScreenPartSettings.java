package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetNumberField;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.WidgetTextFieldDropdown;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartSettings;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Gui for part settings.
 * @author rubensworks
 */
public class ContainerScreenPartSettings extends ContainerScreenExtended<ContainerPartSettings> {

    private WidgetNumberField numberFieldUpdateInterval = null;
    private WidgetNumberField numberFieldPriority = null;
    private WidgetNumberField numberFieldChannel = null;
    private WidgetTextFieldDropdown<Direction> dropdownFieldSide = null;
    private List<SideDropdownEntry> dropdownEntries;

    public ContainerScreenPartSettings(ContainerPartSettings container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    protected void onSave() {
        try {
            if (isFieldSideEnabled()) {
                Direction selectedSide = dropdownFieldSide.getSelectedDropdownPossibility() == null ? null : dropdownFieldSide.getSelectedDropdownPossibility().getValue();
                int side = selectedSide != null && selectedSide != getDefaultSide() ? selectedSide.ordinal() : -1;
                ValueNotifierHelpers.setValue(getContainer(), getContainer().getLastSideValueId(), side);
            }
            if (isFieldUpdateIntervalEnabled()) {
                int updateInterval = numberFieldUpdateInterval.getInt();
                ValueNotifierHelpers.setValue(getContainer(), getContainer().getLastUpdateValueId(), updateInterval);
            }
            if (isFieldPriorityEnabled()) {
                int priority = numberFieldPriority.getInt();
                ValueNotifierHelpers.setValue(getContainer(), getContainer().getLastPriorityValueId(), priority);
            }
            if (isFieldChannelEnabled()) {
                int channel = numberFieldChannel.getInt();
                ValueNotifierHelpers.setValue(getContainer(), getContainer().getLastChannelValueId(), channel);
            }
        } catch (NumberFormatException e) { }
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/part_settings.png");
    }

    protected Direction getCurrentSide() {
        return getContainer().getTarget().getTarget().getSide();
    }

    protected Direction getDefaultSide() {
        return getContainer().getTarget().getCenter().getSide().getOpposite();
    }

    protected String getSideText(Direction side) {
        return side.getName().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);

        if (isFieldSideEnabled()) {
            dropdownEntries = Arrays.stream(Direction.values()).map(SideDropdownEntry::new).collect(Collectors.toList());
            dropdownFieldSide = new WidgetTextFieldDropdown(font, guiLeft + 106, guiTop + getFieldSideY(),
                    70, 14, L10NHelpers.localize("gui.integrateddynamics.partsettings.side"), true,
                    Sets.newHashSet(dropdownEntries));
            setSideInDropdownField(getCurrentSide());
            dropdownFieldSide.setMaxStringLength(15);
            dropdownFieldSide.setVisible(true);
            dropdownFieldSide.setTextColor(16777215);
            dropdownFieldSide.setCanLoseFocus(true);
        }

        if (isFieldUpdateIntervalEnabled()) {
            numberFieldUpdateInterval = new WidgetNumberField(font, guiLeft + 106, guiTop + getFieldUpdateIntervalY(), 70, 14, true,
                    L10NHelpers.localize("gui.integrateddynamics.partsettings.update_interval"), true);
            numberFieldUpdateInterval.setMaxStringLength(15);
            numberFieldUpdateInterval.setVisible(true);
            numberFieldUpdateInterval.setTextColor(16777215);
            numberFieldUpdateInterval.setCanLoseFocus(true);
            numberFieldUpdateInterval.setMinValue(container.getLastMinUpdateValue());
        }

        if (isFieldPriorityEnabled()) {
            numberFieldPriority = new WidgetNumberField(font, guiLeft + 106, guiTop + getFieldPriorityY(), 70, 14, true,
                    L10NHelpers.localize("gui.integrateddynamics.partsettings.priority"), true);
            numberFieldPriority.setPositiveOnly(false);
            numberFieldPriority.setMaxStringLength(15);
            numberFieldPriority.setVisible(true);
            numberFieldPriority.setTextColor(16777215);
            numberFieldPriority.setCanLoseFocus(true);
        }

        if (isFieldChannelEnabled()) {
            numberFieldChannel = new WidgetNumberField(font, guiLeft + 106, guiTop + getFieldChannelY(), 70, 14, true,
                    L10NHelpers.localize("gui.integrateddynamics.partsettings.channel"), true);
            numberFieldChannel.setPositiveOnly(false);
            numberFieldChannel.setMaxStringLength(15);
            numberFieldChannel.setVisible(true);
            numberFieldChannel.setTextColor(16777215);
            numberFieldChannel.setCanLoseFocus(true);
            numberFieldChannel.setEnabled(isChannelEnabled());
        }

        String save = L10NHelpers.localize("gui.integrateddynamics.button.save");
        addButton(new ButtonText(this.guiLeft + 178, this.guiTop + 8, font.getStringWidth(save) + 6, 16, save, save,
                createServerPressable(ContainerPartSettings.BUTTON_SAVE, b -> onSave()), true));

        this.refreshValues();
    }

    protected int getFieldSideY() {
        return 9;
    }

    protected int getFieldUpdateIntervalY() {
        return 34;
    }

    protected int getFieldPriorityY() {
        return 59;
    }

    protected int getFieldChannelY() {
        return 84;
    }

    protected boolean isFieldSideEnabled() {
        return true;
    }

    protected boolean isFieldUpdateIntervalEnabled() {
        return true;
    }

    protected boolean isFieldPriorityEnabled() {
        return true;
    }

    protected boolean isFieldChannelEnabled() {
        return true;
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!(isFieldUpdateIntervalEnabled() && this.numberFieldUpdateInterval.charTyped(typedChar, keyCode))
                && !(isFieldPriorityEnabled() && this.numberFieldPriority.charTyped(typedChar, keyCode))
                && !(isFieldChannelEnabled() && this.numberFieldChannel.charTyped(typedChar, keyCode))
                && !(isFieldSideEnabled() && this.dropdownFieldSide.charTyped(typedChar, keyCode))) {
            return super.charTyped(typedChar, keyCode);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (typedChar != GLFW.GLFW_KEY_ESCAPE) {
            if (isFieldSideEnabled()) {
                if (this.dropdownFieldSide.keyPressed(typedChar, keyCode, modifiers)) {
                    return true;
                }
            }
            if (isFieldUpdateIntervalEnabled()) {
                if (this.numberFieldUpdateInterval.keyPressed(typedChar, keyCode, modifiers)) {
                    return true;
                }
            }
            if (isFieldPriorityEnabled()) {
                if (this.numberFieldPriority.keyPressed(typedChar, keyCode, modifiers)) {
                    return true;
                }
            }
            if (isFieldChannelEnabled()) {
                if (this.numberFieldChannel.keyPressed(typedChar, keyCode, modifiers)) {
                    return true;
                }
            }
            return true;
        } else {
            return super.keyPressed(typedChar, keyCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isFieldSideEnabled()) {
            if (this.dropdownFieldSide.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        if (isFieldUpdateIntervalEnabled()) {
            if (this.numberFieldUpdateInterval.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        if (isFieldPriorityEnabled()) {
            if (this.numberFieldPriority.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        if (isFieldChannelEnabled()) {
            if (this.numberFieldChannel.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        if (isFieldUpdateIntervalEnabled()) {
            font.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.update_interval"), guiLeft + 8, guiTop + getFieldUpdateIntervalY() + 3, Helpers.RGBToInt(0, 0, 0));
            numberFieldUpdateInterval.render(mouseX, mouseY, partialTicks);
        }
        if (isFieldPriorityEnabled()) {
            font.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.priority"), guiLeft + 8, guiTop + getFieldPriorityY() + 3, Helpers.RGBToInt(0, 0, 0));
            numberFieldPriority.render(mouseX, mouseY, partialTicks);
        }
        if (isFieldChannelEnabled()) {
            font.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.channel"), guiLeft + 8, guiTop + getFieldChannelY() + 3, isChannelEnabled() ? Helpers.RGBToInt(0, 0, 0) : Helpers.RGBToInt(100, 100, 100));
            numberFieldChannel.render(mouseX, mouseY, partialTicks);
        }
        if (isFieldSideEnabled()) {
            font.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.side"), guiLeft + 8, guiTop + getFieldSideY() + 3, Helpers.RGBToInt(0, 0, 0));
            dropdownFieldSide.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (!isChannelEnabled()) {
            GuiHelpers.renderTooltip(this, 8, 87, 100, 20, mouseX, mouseY,
                    () -> Lists.<ITextComponent>newArrayList(new TranslationTextComponent("gui.integrateddynamics.partsettings.channel.disabledinfo")));
        }
    }

    protected boolean isChannelEnabled() {
        return GeneralConfig.energyConsumptionMultiplier > 0;
    }

    @Override
    protected int getBaseXSize() {
        return 214;
    }

    @Override
    protected int getBaseYSize() {
        return 191;
    }

    protected void setSideInDropdownField(Direction side) {
        dropdownFieldSide.selectPossibility(dropdownEntries.get(side.ordinal()));
    }

    @Override
    public void onUpdate(int valueId, CompoundNBT value) {
        if (isFieldSideEnabled() && valueId == getContainer().getLastSideValueId()) {
            int side = getContainer().getLastSideValue();
            setSideInDropdownField(side == -1 ? getDefaultSide() : Direction.values()[side]);
        }
        if (isFieldUpdateIntervalEnabled() && valueId == getContainer().getLastUpdateValueId()) {
            numberFieldUpdateInterval.setText(Integer.toString(getContainer().getLastUpdateValue()));
        }
        if (isFieldUpdateIntervalEnabled() && valueId == getContainer().getLastMinUpdateValueId()) {
            numberFieldUpdateInterval.setMinValue(getContainer().getLastMinUpdateValue());
        }
        if (isFieldPriorityEnabled() && valueId == getContainer().getLastPriorityValueId()) {
            numberFieldPriority.setText(Integer.toString(getContainer().getLastPriorityValue()));
        }
        if (isFieldChannelEnabled() && valueId == getContainer().getLastChannelValueId()) {
            numberFieldChannel.setText(Integer.toString(getContainer().getLastChannelValue()));
        }
    }

    public class SideDropdownEntry implements IDropdownEntry<Direction> {

        private final Direction side;

        public SideDropdownEntry(Direction side) {
            this.side = side;
        }

        @Override
        public String getMatchString() {
            return getSideText(side);
        }

        @Override
        public String getDisplayString() {
            return (getDefaultSide() == this.side ? TextFormatting.YELLOW : "") + getMatchString();
        }

        @Override
        public List<ITextComponent> getTooltip() {
            return Collections.emptyList();
        }

        @Override
        public Direction getValue() {
            return this.side;
        }
    }

}
