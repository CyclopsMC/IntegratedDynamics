package org.cyclops.integrateddynamics.core.client.gui.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartSettings;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Gui for part settings.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class GuiPartSettings extends GuiContainerExtended {

    public static final int BUTTON_SAVE = 0;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;

    private GuiNumberField numberFieldUpdateInterval = null;
    private GuiNumberField numberFieldPriority = null;

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

        putButtonAction(BUTTON_SAVE, new IButtonActionClient<GuiContainerExtended, ExtendedInventoryContainer>() {
            @Override
            public void onAction(int buttonId, GuiContainerExtended gui, ExtendedInventoryContainer container) {
                IntegratedDynamics._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, getTarget().getCenter().getSide());
                try {
                    int updateInterval = numberFieldUpdateInterval.getInt();
                    int priority = numberFieldPriority.getInt();
                    ValueNotifierHelpers.setValue(getContainer(), ((ContainerPartSettings) getContainer()).getLastUpdateValueId(), updateInterval);
                    ValueNotifierHelpers.setValue(getContainer(), ((ContainerPartSettings) getContainer()).getLastPriorityValueId(), priority);
                } catch (NumberFormatException e) { }
            }
        });
    }

    @Override
    public String getGuiTexture() {
        return getContainer().getGuiProvider().getModGui().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "part_settings.png";
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        numberFieldUpdateInterval = new GuiNumberField(0, Minecraft.getMinecraft().fontRendererObj, guiLeft + 68, guiTop + 9, 70, 14, true, true);
        numberFieldUpdateInterval.setMaxStringLength(64);
        numberFieldUpdateInterval.setMaxStringLength(15);
        numberFieldUpdateInterval.setVisible(true);
        numberFieldUpdateInterval.setTextColor(16777215);
        numberFieldUpdateInterval.setCanLoseFocus(true);

        numberFieldPriority = new GuiNumberField(0, Minecraft.getMinecraft().fontRendererObj, guiLeft + 68, guiTop + 34, 70, 14, true, true);
        numberFieldPriority.setPositiveOnly(false);
        numberFieldPriority.setMaxStringLength(64);
        numberFieldPriority.setMaxStringLength(15);
        numberFieldPriority.setVisible(true);
        numberFieldPriority.setTextColor(16777215);
        numberFieldPriority.setCanLoseFocus(true);

        String save = L10NHelpers.localize("gui.integrateddynamics.button.save");
        buttonList.add(new GuiButtonText(BUTTON_SAVE, this.guiLeft + 140, this.guiTop + 8, fontRendererObj.getStringWidth(save) + 6, 16, save, true));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.checkHotbarKeys(keyCode)) {
            if (!this.numberFieldUpdateInterval.textboxKeyTyped(typedChar, keyCode)
                    && !this.numberFieldPriority.textboxKeyTyped(typedChar, keyCode)) {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.numberFieldUpdateInterval.mouseClicked(mouseX, mouseY, mouseButton);
        this.numberFieldPriority.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        numberFieldUpdateInterval.drawTextBox(Minecraft.getMinecraft(), mouseX - guiLeft, mouseY - guiTop);
        numberFieldPriority.drawTextBox(Minecraft.getMinecraft(), mouseX - guiLeft, mouseY - guiTop);
        fontRendererObj.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.update_interval"), guiLeft + 8, guiTop + 12, Helpers.RGBToInt(0, 0, 0));
        fontRendererObj.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.priority"), guiLeft + 8, guiTop + 37, Helpers.RGBToInt(0, 0, 0));
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        if (valueId == ((ContainerPartSettings) getContainer()).getLastUpdateValueId()) {
            numberFieldUpdateInterval.setText(Integer.toString(((ContainerPartSettings) getContainer()).getLastUpdateValue()));
        }
        if (valueId == ((ContainerPartSettings) getContainer()).getLastPriorityValueId()) {
            numberFieldPriority.setText(Integer.toString(((ContainerPartSettings) getContainer()).getLastPriorityValue()));
        }
    }

}
