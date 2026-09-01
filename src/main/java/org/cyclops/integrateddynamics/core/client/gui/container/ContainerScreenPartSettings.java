package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetNumberField;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.IModHelpers;
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
    private ButtonText buttonSave = null;

    public ContainerScreenPartSettings(T container, Inventory inventory, Component title) {
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
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/part_settings.png");
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

        if (isFieldSideEnabled()) {
            dropdownEntries = Arrays.stream(Direction.values()).map(SideDropdownEntry::new).collect(Collectors.toList());
            dropdownFieldSide = new WidgetTextFieldDropdown(font, leftPos + 106, topPos + getFieldSideY(),
                    70, 14, Component.translatable("gui.integrateddynamics.partsettings.side"), true,
                    Sets.newHashSet(dropdownEntries));
            setSideInDropdownField(getCurrentSide());
            dropdownFieldSide.setMaxLength(15);
            dropdownFieldSide.setVisible(true);
            dropdownFieldSide.setTextColor(ARGB.opaque(16777215));
            dropdownFieldSide.setCanLoseFocus(true);
        }

        if (isFieldUpdateIntervalEnabled()) {
            numberFieldUpdateInterval = new WidgetNumberField(font, leftPos + 106, topPos + getFieldUpdateIntervalY(), 70, 14, true,
                    Component.translatable("gui.integrateddynamics.partsettings.update_interval"), true);
            numberFieldUpdateInterval.setMaxLength(15);
            numberFieldUpdateInterval.setVisible(true);
            numberFieldUpdateInterval.setTextColor(ARGB.opaque(16777215));
            numberFieldUpdateInterval.setCanLoseFocus(true);
            numberFieldUpdateInterval.setMinValue(container.getLastMinUpdateValue());
        }

        if (isFieldPriorityEnabled()) {
            numberFieldPriority = new WidgetNumberField(font, leftPos + 106, topPos + getFieldPriorityY(), 70, 14, true,
                    Component.translatable("gui.integrateddynamics.partsettings.priority"), true);
            numberFieldPriority.setPositiveOnly(false);
            numberFieldPriority.setMaxLength(15);
            numberFieldPriority.setVisible(true);
            numberFieldPriority.setTextColor(ARGB.opaque(16777215));
            numberFieldPriority.setCanLoseFocus(true);
        }

        if (isFieldChannelEnabled()) {
            numberFieldChannel = new WidgetNumberField(font, leftPos + 106, topPos + getFieldChannelY(), 70, 14, true,
                    Component.translatable("gui.integrateddynamics.partsettings.channel"), true);
            numberFieldChannel.setPositiveOnly(false);
            numberFieldChannel.setMaxLength(15);
            numberFieldChannel.setVisible(true);
            numberFieldChannel.setTextColor(ARGB.opaque(16777215));
            numberFieldChannel.setCanLoseFocus(true);
            numberFieldChannel.setEditable(isChannelEnabled());
        }

        MutableComponent save = Component.translatable("gui.integrateddynamics.button.save");
        addRenderableWidget(buttonSave = new ButtonText(this.leftPos + 178, this.topPos + 8, font.width(save.getVisualOrderText()) + 6, 16, save, save,
                createServerPressable(ContainerPartSettings.BUTTON_SAVE, b -> onSave()), true));

        this.refreshValues();
    }

    @Override
    public void onClose() {
        // Auto-save the settings when the gui is closed,
        // so that players don't have to explicitly confirm their changes.
        onSave();
        super.onClose();
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
    public boolean charTyped(CharacterEvent evt) {
        if (!(isFieldUpdateIntervalEnabled() && this.numberFieldUpdateInterval.charTyped(evt))
                && !(isFieldPriorityEnabled() && this.numberFieldPriority.charTyped(evt))
                && !(isFieldChannelEnabled() && this.numberFieldChannel.charTyped(evt))
                && !(isFieldSideEnabled() && this.dropdownFieldSide.charTyped(evt))) {
            return super.charTyped(evt);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (evt.key() != GLFW.GLFW_KEY_ESCAPE) {
            if (isFieldSideEnabled()) {
                if (this.dropdownFieldSide.keyPressed(evt)) {
                    return true;
                }
            }
            if (isFieldUpdateIntervalEnabled()) {
                if (this.numberFieldUpdateInterval.keyPressed(evt)) {
                    return true;
                }
            }
            if (isFieldPriorityEnabled()) {
                if (this.numberFieldPriority.keyPressed(evt)) {
                    return true;
                }
            }
            if (isFieldChannelEnabled()) {
                if (this.numberFieldChannel.keyPressed(evt)) {
                    return true;
                }
            }
            return true;
        } else {
            // Don't close all GUIs, but go back to the part's GUI.
            exitToPartGui(evt);
            return true;
        }
    }

    /**
     * Save the current settings and go back to the GUI of the part.
     */
    protected void exitToPartGui(InputWithModifiers input) {
        buttonSave.onPress(input);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        if (isFieldSideEnabled()) {
            if (this.dropdownFieldSide.mouseClicked(evt, isDoubleClick)) {
                return true;
            }
        }
        if (isFieldUpdateIntervalEnabled()) {
            if (this.numberFieldUpdateInterval.mouseClicked(evt, isDoubleClick)) {
                return true;
            }
        }
        if (isFieldPriorityEnabled()) {
            if (this.numberFieldPriority.mouseClicked(evt, isDoubleClick)) {
                return true;
            }
        }
        if (isFieldChannelEnabled()) {
            if (this.numberFieldChannel.mouseClicked(evt, isDoubleClick)) {
                return true;
            }
        }
        return super.mouseClicked(evt, isDoubleClick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (isFieldUpdateIntervalEnabled()) {
            guiGraphics.text(font, IModHelpers.get().getL10NHelpers().localize("gui.integrateddynamics.partsettings.update_interval"), leftPos + 8, topPos + getFieldUpdateIntervalY() + 3, ARGB.opaque(0), false);
            numberFieldUpdateInterval.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }
        if (isFieldPriorityEnabled()) {
            guiGraphics.text(font, IModHelpers.get().getL10NHelpers().localize("gui.integrateddynamics.partsettings.priority"), leftPos + 8, topPos + getFieldPriorityY() + 3, ARGB.opaque(0), false);
            numberFieldPriority.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }
        if (isFieldChannelEnabled()) {
            guiGraphics.text(font, IModHelpers.get().getL10NHelpers().localize("gui.integrateddynamics.partsettings.channel"), leftPos + 8, topPos + getFieldChannelY() + 3, isChannelEnabled() ? ARGB.opaque(0) : IModHelpers.get().getBaseHelpers().RGBAToInt(100, 100, 100, 255), false);
            numberFieldChannel.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }
        if (isFieldSideEnabled()) {
            guiGraphics.text(font, IModHelpers.get().getL10NHelpers().localize("gui.integrateddynamics.partsettings.side"), leftPos + 8, topPos + getFieldSideY() + 3, ARGB.opaque(0), false);
            dropdownFieldSide.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        if (!isChannelEnabled()) {
            IModHelpers.get().getGuiHelpers().renderTooltip(this, guiGraphics, 8, getFieldChannelY() + 3, 100, 20, mouseX, mouseY,
                    () -> Lists.<Component>newArrayList(Component.translatable("gui.integrateddynamics.partsettings.channel.disabledinfo")));
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
    public void onUpdate(int valueId, CompoundTag value) {
        if (isFieldSideEnabled() && valueId == getMenu().getLastSideValueId()) {
            int side = getMenu().getLastSideValue();
            setSideInDropdownField(side == -1 ? getDefaultSide() : Direction.values()[side]);
        }
        if (isFieldUpdateIntervalEnabled() && valueId == getMenu().getLastUpdateValueId()) {
            numberFieldUpdateInterval.setValue(Integer.toString(getMenu().getLastUpdateValue()));
        }
        if (isFieldUpdateIntervalEnabled() && valueId == getMenu().getLastMinUpdateValueId()) {
            numberFieldUpdateInterval.setMinValue(getMenu().getLastMinUpdateValue());
        }
        if (isFieldPriorityEnabled() && valueId == getMenu().getLastPriorityValueId()) {
            numberFieldPriority.setValue(Integer.toString(getMenu().getLastPriorityValue()));
        }
        if (isFieldChannelEnabled() && valueId == getMenu().getLastChannelValueId()) {
            numberFieldChannel.setValue(Integer.toString(getMenu().getLastChannelValue()));
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
        public MutableComponent getDisplayString() {
            if (getDefaultSide() == this.side) {
                return Component.literal(getMatchString()).withStyle(ChatFormatting.YELLOW);
            }
            return Component.literal(getMatchString());
        }

        @Override
        public List<MutableComponent> getTooltip() {
            return Collections.emptyList();
        }

        @Override
        public Direction getValue() {
            return this.side;
        }
    }

}
