package org.cyclops.integrateddynamics.core.client.gui.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.GuiNumberField;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
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

    private static final int BUTTON_SAVE = 0;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;

    private GuiNumberField numberField = null;

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
    }

    @Override
    public String getGuiTexture() {
        return getContainer().getGuiProvider().getMod().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "partSettings.png";
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        numberField = new GuiNumberField(0, Minecraft.getMinecraft().fontRendererObj, guiLeft + 38, guiTop + 9, 100, 14, true, true);
        numberField.setMaxStringLength(64);
        numberField.setMaxStringLength(15);
        numberField.setVisible(true);
        numberField.setTextColor(16777215);
        numberField.setCanLoseFocus(true);

        String save = L10NHelpers.localize("gui.integrateddynamics.button.save");
        buttonList.add(new GuiButtonText(BUTTON_SAVE, this.guiLeft + 140,  this.guiTop + 8, fontRendererObj.getStringWidth(save) + 6, 16 , save, true));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.checkHotbarKeys(keyCode)) {
            if (!this.numberField.textboxKeyTyped(typedChar, keyCode)) {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.numberField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        numberField.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
        fontRendererObj.drawString(L10NHelpers.localize("gui.integrateddynamics.partsettings.updateInterval"), guiLeft + 8, guiTop + 12, Helpers.RGBToInt(0, 0, 0));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if(guibutton.id == BUTTON_SAVE) {
            try {
                int updateInterval = numberField.getInt();
                ValueNotifierHelpers.setValue(getContainer(), ((ContainerPartSettings) getContainer()).getLastUpdateValueId(), updateInterval);
            } catch (NumberFormatException e) { }
        }
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        numberField.setText(Integer.toString(((ContainerPartSettings) getContainer()).getLastUpdateValue()));
    }

}
