package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.gui.ContainerScreenActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerMaterializer;
import org.cyclops.integrateddynamics.network.packet.MaterializerCopyValuePacket;
import org.lwjgl.glfw.GLFW;

/**
 * Gui for the proxy.
 * @author rubensworks
 */
public class ContainerScreenMaterializer extends ContainerScreenActiveVariableBase<ContainerMaterializer> {

    private static final int ERROR_X = 110;
    private static final int ERROR_Y = 26;

    // The free area left of the read slot, which starts at x 76 and y 20
    private static final int COPY_X = 8;
    private static final int COPY_Y = 24;
    private static final int COPY_COMPRESSED_X = 8;
    private static final int COPY_COMPRESSED_Y = 38;
    private static final int COPY_WIDTH = 32;
    private static final int COPY_HEIGHT = 12;

    public ContainerScreenMaterializer(ContainerMaterializer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new ButtonText(getGuiLeftTotal() + COPY_X, getGuiTopTotal() + COPY_Y, COPY_WIDTH, COPY_HEIGHT,
                Component.translatable("gui.integrateddynamics.button.copy"),
                Component.translatable("gui.integrateddynamics.button.copy"),
                (button) -> valueToClipboard(false), true));
        addRenderableWidget(new ButtonText(getGuiLeftTotal() + COPY_COMPRESSED_X, getGuiTopTotal() + COPY_COMPRESSED_Y, COPY_WIDTH, COPY_HEIGHT,
                Component.translatable("gui.integrateddynamics.button.copy_compressed"),
                Component.translatable("gui.integrateddynamics.button.copy_compressed"),
                (button) -> valueToClipboard(true), true));
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/materializer.png");
    }

    @Override
    protected int getBaseYSize() {
        return 189;
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
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        GuiHelpers.renderTooltip(this, guiGraphics.pose(), COPY_X, COPY_Y, COPY_WIDTH, COPY_HEIGHT, mouseX, mouseY,
                () -> Lists.newArrayList(Component.translatable("gui.integrateddynamics.button.copy.info")));
        GuiHelpers.renderTooltip(this, guiGraphics.pose(), COPY_COMPRESSED_X, COPY_COMPRESSED_Y, COPY_WIDTH, COPY_HEIGHT, mouseX, mouseY,
                () -> Lists.newArrayList(Component.translatable("gui.integrateddynamics.button.copy_compressed.info")));
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (GLFW.GLFW_KEY_C == keyCode && KeyModifier.CONTROL.isActive(KeyConflictContext.GUI)) {
            valueToClipboard(false);
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    /**
     * Ask the server for the materialized value, which is then placed on the clipboard.
     * @param compressed If the compressed form must be used instead of the human-readable one.
     */
    protected void valueToClipboard(boolean compressed) {
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new MaterializerCopyValuePacket(compressed));
    }
}
