package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.GuiNumberField;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.cyclopscore.inventory.container.button.IButtonActionClient;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.client.gui.GuiTextFieldDropdown;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartSettings;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Gui for part settings.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class GuiPartSettings extends GuiContainerExtended {

    public static final int BUTTON_SAVE = 0;
    public static final int BUTTON_EXPOSE_CAPABILITIES = 1;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;

    private GuiNumberField numberFieldUpdateInterval = null;
    private GuiNumberField numberFieldPriority = null;
    private GuiNumberField numberFieldChannel = null;
    private GuiTextFieldDropdown<EnumFacing> dropdownFieldSide = null;
    private List<SideDropdownEntry> dropdownEntries;
    private GuiButtonText buttonExposeCapabilities = null;
    private boolean exposeCapabilities = false;

    private final String trueStr = L10NHelpers.localize("general.integrateddynamics.true");
    private final String falseStr = L10NHelpers.localize("general.integrateddynamics.false");

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     */
    public GuiPartSettings(EntityPlayer player, PartTarget target, IPartContainer partContainer, IPartType partType) {
        super(new ContainerPartSettings(player, target, partContainer, partType));
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;

        putButtonAction(BUTTON_SAVE, (buttonId, gui, container) -> {
            IntegratedDynamics._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, getTarget().getCenter().getSide());
            try {
                int updateInterval = numberFieldUpdateInterval.getInt();
                int priority = numberFieldPriority.getInt();
                int channel = numberFieldChannel.getInt();
                EnumFacing selectedSide = dropdownFieldSide.getSelectedDropdownPossibility() == null ? null : dropdownFieldSide.getSelectedDropdownPossibility().getValue();
                int side = selectedSide != null && selectedSide != getDefaultSide() ? selectedSide.ordinal() : -1;
                ValueNotifierHelpers.setValue(getContainer(), ((ContainerPartSettings) getContainer()).getLastUpdateValueId(), updateInterval);
                ValueNotifierHelpers.setValue(getContainer(), ((ContainerPartSettings) getContainer()).getLastPriorityValueId(), priority);
                ValueNotifierHelpers.setValue(getContainer(), ((ContainerPartSettings) getContainer()).getLastChannelValueId(), channel);
                ValueNotifierHelpers.setValue(getContainer(), ((ContainerPartSettings) getContainer()).getLastSideValueId(), side);
                ValueNotifierHelpers.setValue(getContainer(), ((ContainerPartSettings) getContainer()).getLastExposeCapabilitiesValueId(), exposeCapabilities ? 1 : 0);
            } catch (NumberFormatException e) { }
        });

        putButtonAction(BUTTON_EXPOSE_CAPABILITIES, (buttonId, gui, container) -> {
            updateExposeCapabilities(!exposeCapabilities);
        });
    }

    @Override
    public String getGuiTexture() {
        return getContainer().getGuiProvider().getModGui().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "part_settings.png";
    }

    protected EnumFacing getCurrentSide() {
        return getTarget().getTarget().getSide();
    }

    protected EnumFacing getDefaultSide() {
        return getTarget().getCenter().getSide().getOpposite();
    }

    protected String getSideText(EnumFacing side) {
        return side.getName().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        dropdownEntries = Arrays.stream(EnumFacing.VALUES).map(SideDropdownEntry::new).collect(Collectors.toList());
        dropdownFieldSide = new GuiTextFieldDropdown(0, Minecraft.getMinecraft().fontRenderer, guiLeft + 106, guiTop + 9,
                70, 14, true, Sets.newHashSet(dropdownEntries));
        setSideInDropdownField(getCurrentSide());
        dropdownFieldSide.setMaxStringLength(15);
        dropdownFieldSide.setVisible(true);
        dropdownFieldSide.setTextColor(16777215);
        dropdownFieldSide.setCanLoseFocus(true);

        numberFieldUpdateInterval = new GuiNumberField(0, Minecraft.getMinecraft().fontRenderer, guiLeft + 106, guiTop + 34, 70, 14, true, true);
        numberFieldUpdateInterval.setMaxStringLength(15);
        numberFieldUpdateInterval.setVisible(true);
        numberFieldUpdateInterval.setTextColor(16777215);
        numberFieldUpdateInterval.setCanLoseFocus(true);

        numberFieldPriority = new GuiNumberField(0, Minecraft.getMinecraft().fontRenderer, guiLeft + 106, guiTop + 59, 70, 14, true, true);
        numberFieldPriority.setPositiveOnly(false);
        numberFieldPriority.setMaxStringLength(15);
        numberFieldPriority.setVisible(true);
        numberFieldPriority.setTextColor(16777215);
        numberFieldPriority.setCanLoseFocus(true);

        numberFieldChannel = new GuiNumberField(0, Minecraft.getMinecraft().fontRenderer, guiLeft + 106, guiTop + 84, 70, 14, true, true);
        numberFieldChannel.setPositiveOnly(false);
        numberFieldChannel.setMaxStringLength(15);
        numberFieldChannel.setVisible(true);
        numberFieldChannel.setTextColor(16777215);
        numberFieldChannel.setCanLoseFocus(true);

        buttonExposeCapabilities = new GuiButtonText(BUTTON_EXPOSE_CAPABILITIES, guiLeft + 106, guiTop + 108, 68, 16, "", true);
        buttonList.add(buttonExposeCapabilities);

        String save = L10NHelpers.localize("gui.integrateddynamics.button.save");
        buttonList.add(new GuiButtonText(BUTTON_SAVE, this.guiLeft + 178, this.guiTop + 8, fontRenderer.getStringWidth(save) + 6, 16, save, true));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.checkHotbarKeys(keyCode)) {
            if (!this.numberFieldUpdateInterval.textboxKeyTyped(typedChar, keyCode)
                    && !this.numberFieldPriority.textboxKeyTyped(typedChar, keyCode)
                    && !this.numberFieldChannel.textboxKeyTyped(typedChar, keyCode)
                    && !this.dropdownFieldSide.textboxKeyTyped(typedChar, keyCode)) {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.numberFieldUpdateInterval.mouseClicked(mouseX, mouseY, mouseButton);
        this.numberFieldPriority.mouseClicked(mouseX, mouseY, mouseButton);
        this.numberFieldChannel.mouseClicked(mouseX, mouseY, mouseButton);
        this.dropdownFieldSide.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        numberFieldUpdateInterval.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
        numberFieldPriority.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
        numberFieldChannel.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
        dropdownFieldSide.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
        fontRenderer.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.side"), guiLeft + 8, guiTop + 12, Helpers.RGBToInt(0, 0, 0));
        fontRenderer.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.update_interval"), guiLeft + 8, guiTop + 37, Helpers.RGBToInt(0, 0, 0));
        fontRenderer.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.priority"), guiLeft + 8, guiTop + 62, Helpers.RGBToInt(0, 0, 0));
        fontRenderer.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.channel"), guiLeft + 8, guiTop + 87, Helpers.RGBToInt(0, 0, 0));
        fontRenderer.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.expose_capabilities"), guiLeft + 8, guiTop + 112, Helpers.RGBToInt(0, 0, 0));
    }

    @Override
    protected int getBaseXSize() {
        return 214;
    }

    @Override
    protected int getBaseYSize() {
        return 214;
    }

    protected void setSideInDropdownField(EnumFacing side) {
        dropdownFieldSide.selectPossibility(dropdownEntries.get(side.ordinal()));
    }

    protected void updateExposeCapabilities(boolean newValue) {
        exposeCapabilities = newValue;
        buttonExposeCapabilities.displayString = newValue ? trueStr : falseStr;
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        if (valueId == ((ContainerPartSettings) getContainer()).getLastUpdateValueId()) {
            numberFieldUpdateInterval.setText(Integer.toString(((ContainerPartSettings) getContainer()).getLastUpdateValue()));
        }
        if (valueId == ((ContainerPartSettings) getContainer()).getLastPriorityValueId()) {
            numberFieldPriority.setText(Integer.toString(((ContainerPartSettings) getContainer()).getLastPriorityValue()));
        }
        if (valueId == ((ContainerPartSettings) getContainer()).getLastChannelValueId()) {
            numberFieldChannel.setText(Integer.toString(((ContainerPartSettings) getContainer()).getLastChannelValue()));
        }
        if (valueId == ((ContainerPartSettings) getContainer()).getLastSideValueId()) {
            int side = ((ContainerPartSettings) getContainer()).getLastSideValue();
            setSideInDropdownField(side == -1 ? getDefaultSide() : EnumFacing.VALUES[side]);
        }
        if (valueId == ((ContainerPartSettings) getContainer()).getLastExposeCapabilitiesValueId()) {
            updateExposeCapabilities(((ContainerPartSettings) getContainer()).getLastExposeCapabilitiesValue());
        }
    }

    public class SideDropdownEntry implements IDropdownEntry<EnumFacing> {

        private final EnumFacing side;

        public SideDropdownEntry(EnumFacing side) {
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
        public List<String> getTooltip() {
            return Collections.emptyList();
        }

        @Override
        public EnumFacing getValue() {
            return this.side;
        }
    }

}
