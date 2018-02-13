package org.cyclops.integrateddynamics.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiMultipart;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartDisplay;
import org.lwjgl.input.Keyboard;

import java.io.IOException;


/**
 * Gui for a writer part.
 * @author rubensworks
 */
public class GuiPartDisplay<P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> extends GuiMultipart<P, S> {

    private static final int ERROR_X = 104;
    private static final int ERROR_Y = 16;
    private static final int OK_X = 104;
    private static final int OK_Y = 16;

    private static final int BUTTON_COPY = 0;

    /**
     * Make a new instance.
     * @param partTarget The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The targeted part type.
     */
    public GuiPartDisplay(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, IPartType partType) {
        super(new ContainerPartDisplay<P, S>(player, partTarget, partContainer, partType));
    }

    @Override
    public void initGui() {
        super.initGui();

        addButton(new GuiButtonText(BUTTON_COPY, getGuiLeft() + 128, getGuiTop() + 32, 30, 12, L10NHelpers.localize("gui.integrateddynamics.button.copy"), true));
    }

    @Override
    protected String getNameId() {
        return "part_display";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        String readValue = ((ContainerPartDisplay<?, ?>) getContainer()).getReadValue();
        int readValueColor = ((ContainerPartDisplay<?, ?>) getContainer()).getReadValueColor();
        boolean ok = false;
        if(readValue != null) {
            ok = true;
            RenderHelpers.drawScaledCenteredString(fontRenderer, readValue,
                    getGuiLeft() + 53, getGuiTop() + 38, 70, readValueColor);
        }

        GlStateManager.color(1, 1, 1);
        displayErrors.drawBackground(getPartState().getGlobalErrors(), ERROR_X, ERROR_Y, OK_X, OK_Y, this,
                this.guiLeft, this.guiTop, ok);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        // Render error tooltip
        displayErrors.drawForeground(getPartState().getGlobalErrors(), ERROR_X, ERROR_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);

        // Draw tooltip over copy button
        GuiHelpers.renderTooltip(this, 128, 32, 30, 12, mouseX, mouseY,
                () -> Lists.newArrayList(L10NHelpers.localize("gui.integrateddynamics.button.copy.info")));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (Keyboard.KEY_C == keyCode && KeyModifier.CONTROL.isActive(KeyConflictContext.GUI)) {
            valueToClipboard();
        }
    }

    @Override
    protected int getBaseXSize() {
        return 176;
    }

    @Override
    protected int getBaseYSize() {
        return 128;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == BUTTON_COPY) {
            valueToClipboard();
        }
    }

    protected void valueToClipboard() {
        String readValue = ((ContainerPartDisplay<?, ?>) getContainer()).getReadValue();
        if (readValue != null) {
            setClipboardString(readValue);
        }
    }
}
