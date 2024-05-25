package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.core.client.gui.container.ContainerScreenMultipart;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartPanelVariableDriven;
import org.lwjgl.glfw.GLFW;


/**
 * Gui for a writer part.
 * @author rubensworks
 */
public class ContainerScreenPartDisplay<P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>>
        extends ContainerScreenMultipart<P, S, ContainerPartPanelVariableDriven<P, S>> {

    private static final int ERROR_X = 104;
    private static final int ERROR_Y = 16;
    private static final int OK_X = 104;
    private static final int OK_Y = 16;

    public ContainerScreenPartDisplay(ContainerPartPanelVariableDriven<P, S> container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new ButtonText(getGuiLeftTotal() + 128, getGuiTopTotal() + 32, 30, 12,
                Component.translatable("gui.integrateddynamics.button.copy"),
                Component.translatable("gui.integrateddynamics.button.copy"),
                (button) -> valueToClipboard(), true));
    }

    @Override
    protected String getNameId() {
        return "part_display";
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);

        Component readValue = getMenu().getReadValue();
        int readValueColor = getMenu().getReadValueColor();
        boolean ok = false;
        if(readValue != null) {
            ok = true;
            RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), font, readValue.getString(),
                    getGuiLeftTotal() + 53, getGuiTopTotal() + 38, 70, readValueColor, false, Font.DisplayMode.NORMAL);
        }

        displayErrors.drawBackground(guiGraphics, getMenu().getReadErrors(), ERROR_X, ERROR_Y, OK_X, OK_Y, this,
                this.leftPos, this.topPos, ok);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        // Render error tooltip
        displayErrors.drawForeground(guiGraphics.pose(), getMenu().getReadErrors(), ERROR_X, ERROR_Y, mouseX, mouseY, this, this.leftPos, this.topPos);

        // Draw tooltip over copy button
        GuiHelpers.renderTooltip(this, guiGraphics.pose(), 128, 32, 30, 12, mouseX, mouseY,
                () -> Lists.newArrayList(Component.translatable("gui.integrateddynamics.button.copy.info")));
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (GLFW.GLFW_KEY_C == keyCode && KeyModifier.CONTROL.isActive(KeyConflictContext.GUI)) {
            valueToClipboard();
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    protected int getBaseXSize() {
        return 176;
    }

    @Override
    protected int getBaseYSize() {
        return 128;
    }

    protected void valueToClipboard() {
        Component readValue = getMenu().getReadValue();
        if (readValue != null) {
            getMinecraft().keyboardHandler.setClipboard(readValue.getString());
        }
    }
}
