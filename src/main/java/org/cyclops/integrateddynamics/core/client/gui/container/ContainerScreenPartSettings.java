package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
public class ContainerScreenPartSettings<T extends ContainerPartSettings> extends ContainerScreenExtended<T> {

    private WidgetNumberField numberFieldUpdateInterval = null;
    private WidgetNumberField numberFieldPriority = null;
    private WidgetNumberField numberFieldChannel = null;
    private WidgetTextFieldDropdown<Direction> dropdownFieldSide = null;
    private List<SideDropdownEntry> dropdownEntries;

    public ContainerScreenPartSettings(T container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    protected void onSave() {
        try {
            if (isFieldSideEnabled()) {
                Direction selectedSide = dropdownFieldSide.getSelectedDropdownPossibility() == null ? null : dropdownFieldSide.getSelectedDropdownPossibility().getValue();
                int side = selectedSide != null && selectedSide != getDefaultSide() ? selectedSide.ordinal() : -1;
                ValueNotifierHelpers.setValue(getMenu(), getMenu().getLastSideValueId(), side);
            }
            if (isFieldUpdateIntervalEnabled()) {
                int updateInterval = numberFieldUpdateInterval.getInt();
                ValueNotifierHelpers.setValue(getMenu(), getMenu().getLastUpdateValueId(), updateInterval);
            }
            if (isFieldPriorityEnabled()) {
                int priority = numberFieldPriority.getInt();
                ValueNotifierHelpers.setValue(getMenu(), getMenu().getLastPriorityValueId(), priority);
            }
            if (isFieldChannelEnabled()) {
                int channel = numberFieldChannel.getInt();
                ValueNotifierHelpers.setValue(getMenu(), getMenu().getLastChannelValueId(), channel);
            }
        } catch (NumberFormatException e) { }
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/part_settings.png");
    }

    protected Direction getCurrentSide() {
        return getMenu().getTarget().getTarget().getSide();
    }

    protected Direction getDefaultSide() {
        return getMenu().getTarget().getCenter().getSide().getOpposite();
    }

    protected String getSideText(Direction side) {
        return side.getSerializedName().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public void init() {
        super.init();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);

        if (isFieldSideEnabled()) {
            dropdownEntries = Arrays.stream(Direction.values()).map(SideDropdownEntry::new).collect(Collectors.toList());
            dropdownFieldSide = new WidgetTextFieldDropdown(font, leftPos + 106, topPos + getFieldSideY(),
                    70, 14, new TranslationTextComponent("gui.integrateddynamics.partsettings.side"), true,
                    Sets.newHashSet(dropdownEntries));
            setSideInDropdownField(getCurrentSide());
            dropdownFieldSide.setMaxLength(15);
            dropdownFieldSide.setVisible(true);
            dropdownFieldSide.setTextColor(16777215);
            dropdownFieldSide.setCanLoseFocus(true);
        }

        if (isFieldUpdateIntervalEnabled()) {
            numberFieldUpdateInterval = new WidgetNumberField(font, leftPos + 106, topPos + getFieldUpdateIntervalY(), 70, 14, true,
                    new TranslationTextComponent("gui.integrateddynamics.partsettings.update_interval"), true);
            numberFieldUpdateInterval.setMaxLength(15);
            numberFieldUpdateInterval.setVisible(true);
            numberFieldUpdateInterval.setTextColor(16777215);
            numberFieldUpdateInterval.setCanLoseFocus(true);
            numberFieldUpdateInterval.setMinValue(container.getLastMinUpdateValue());
        }

        if (isFieldPriorityEnabled()) {
            numberFieldPriority = new WidgetNumberField(font, leftPos + 106, topPos + getFieldPriorityY(), 70, 14, true,
                    new TranslationTextComponent("gui.integrateddynamics.partsettings.priority"), true);
            numberFieldPriority.setPositiveOnly(false);
            numberFieldPriority.setMaxLength(15);
            numberFieldPriority.setVisible(true);
            numberFieldPriority.setTextColor(16777215);
            numberFieldPriority.setCanLoseFocus(true);
        }

        if (isFieldChannelEnabled()) {
            numberFieldChannel = new WidgetNumberField(font, leftPos + 106, topPos + getFieldChannelY(), 70, 14, true,
                    new TranslationTextComponent("gui.integrateddynamics.partsettings.channel"), true);
            numberFieldChannel.setPositiveOnly(false);
            numberFieldChannel.setMaxLength(15);
            numberFieldChannel.setVisible(true);
            numberFieldChannel.setTextColor(16777215);
            numberFieldChannel.setCanLoseFocus(true);
            numberFieldChannel.setEnabled(isChannelEnabled());
        }

        TranslationTextComponent save = new TranslationTextComponent("gui.integrateddynamics.button.save");
        // MCP: getStringWidth
        addButton(new ButtonText(this.leftPos + 178, this.topPos + 8, font.width(save.getVisualOrderText()) + 6, 16, save, save,
                createServerPressable(ContainerPartSettings.BUTTON_SAVE, b -> onSave()), true));

        this.refreshValues();
    }

    @Override
    protected void renderBg(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        // TODO: rm
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
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        if (isFieldUpdateIntervalEnabled()) {
            font.draw(matrixStack, L10NHelpers.localize("gui.integrateddynamics.partsettings.update_interval"), leftPos + 8, topPos + getFieldUpdateIntervalY() + 3, Helpers.RGBToInt(0, 0, 0));
            numberFieldUpdateInterval.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        if (isFieldPriorityEnabled()) {
            font.draw(matrixStack, L10NHelpers.localize("gui.integrateddynamics.partsettings.priority"), leftPos + 8, topPos + getFieldPriorityY() + 3, Helpers.RGBToInt(0, 0, 0));
            numberFieldPriority.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        if (isFieldChannelEnabled()) {
            font.draw(matrixStack, L10NHelpers.localize("gui.integrateddynamics.partsettings.channel"), leftPos + 8, topPos + getFieldChannelY() + 3, isChannelEnabled() ? Helpers.RGBToInt(0, 0, 0) : Helpers.RGBToInt(100, 100, 100));
            numberFieldChannel.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        if (isFieldSideEnabled()) {
            font.draw(matrixStack, L10NHelpers.localize("gui.integrateddynamics.partsettings.side"), leftPos + 8, topPos + getFieldSideY() + 3, Helpers.RGBToInt(0, 0, 0));
            dropdownFieldSide.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
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
        if (isFieldSideEnabled() && valueId == getMenu().getLastSideValueId()) {
            int side = getMenu().getLastSideValue();
            setSideInDropdownField(side == -1 ? getDefaultSide() : Direction.values()[side]);
        }
        if (isFieldUpdateIntervalEnabled() && valueId == getMenu().getLastUpdateValueId()) {
            numberFieldUpdateInterval.setText(Integer.toString(getMenu().getLastUpdateValue()));
        }
        if (isFieldUpdateIntervalEnabled() && valueId == getMenu().getLastMinUpdateValueId()) {
            numberFieldUpdateInterval.setMinValue(getMenu().getLastMinUpdateValue());
        }
        if (isFieldPriorityEnabled() && valueId == getMenu().getLastPriorityValueId()) {
            numberFieldPriority.setText(Integer.toString(getMenu().getLastPriorityValue()));
        }
        if (isFieldChannelEnabled() && valueId == getMenu().getLastChannelValueId()) {
            numberFieldChannel.setText(Integer.toString(getMenu().getLastChannelValue()));
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
        public IFormattableTextComponent getDisplayString() {
            if (getDefaultSide() == this.side) {
                return new StringTextComponent(getMatchString()).withStyle(TextFormatting.YELLOW);
            }
            return new StringTextComponent(getMatchString());
        }

        @Override
        public List<IFormattableTextComponent> getTooltip() {
            return Collections.emptyList();
        }

        @Override
        public Direction getValue() {
            return this.side;
        }
    }

}
