package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
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
                new TranslatableComponent("gui.integrateddynamics.button.copy"),
                new TranslatableComponent("gui.integrateddynamics.button.copy"),
                (button) -> valueToClipboard(), true));
    }

    @Override
    protected String getNameId() {
        return "part_display";
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        Component readValue = getMenu().getReadValue();
        int readValueColor = getMenu().getReadValueColor();
        boolean ok = false;
        if(readValue != null) {
            ok = true;
            RenderHelpers.drawScaledCenteredString(matrixStack, font, readValue.getString(),
                    getGuiLeftTotal() + 53, getGuiTopTotal() + 38, 70, readValueColor);
        }

        displayErrors.drawBackground(matrixStack, getMenu().getReadErrors(), ERROR_X, ERROR_Y, OK_X, OK_Y, this,
                this.leftPos, this.topPos, ok);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);
        // Render error tooltip
        displayErrors.drawForeground(matrixStack, getMenu().getReadErrors(), ERROR_X, ERROR_Y, mouseX, mouseY, this, this.leftPos, this.topPos);

        // Draw tooltip over copy button
        GuiHelpers.renderTooltip(this, matrixStack, 128, 32, 30, 12, mouseX, mouseY,
                () -> Lists.newArrayList(new TranslatableComponent("gui.integrateddynamics.button.copy.info")));
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
