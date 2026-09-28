package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.gui.image.Images;
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

    public ContainerScreenMaterializer(ContainerMaterializer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void init() {
        super.init();

        // Next to the gui, where this mod puts the buttons of a part as well
        addCopyButton(0, Images.BUTTON_MIDDLE_COPY, "gui.integrateddynamics.materializer.copy", false);
        addCopyButton(20, Images.BUTTON_MIDDLE_COPY_COMPRESSED, "gui.integrateddynamics.materializer.copy_compressed", true);
    }

    protected void addCopyButton(int offsetY, IImage image, String tooltipKey, boolean compressed) {
        ButtonImage button = new ButtonImage(this.leftPos - 20, this.topPos + offsetY, 18, 18,
                Component.translatable(tooltipKey), (b) -> valueToClipboard(compressed),
                new IImage[]{Images.BUTTON_BACKGROUND_INACTIVE, image}, false, 0, 0);
        button.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        addRenderableWidget(button);
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
