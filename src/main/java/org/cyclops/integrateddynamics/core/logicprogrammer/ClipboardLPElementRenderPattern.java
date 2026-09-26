package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.VariableClipboardHelpers;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerClipboardValueChangedPacket;

/**
 * Render pattern for the clipboard element, which pastes a value from the clipboard.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ClipboardLPElementRenderPattern extends RenderPattern<ClipboardLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private Button buttonPaste;

    public ClipboardLPElementRenderPattern(ClipboardLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                           ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);
        buttonList.add(buttonPaste = new ButtonText(guiLeft + getX() + 3, guiTop + getY() + 4, 34, 14,
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_CLIPBOARD_PASTE),
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_CLIPBOARD_PASTE), b -> {}, true));
    }

    @Override
    protected void actionPerformed(Button guibutton) {
        super.actionPerformed(guibutton);
        if (guibutton == buttonPaste) {
            paste();
        }
    }

    protected void paste() {
        String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
        CompoundTag tag;
        try {
            tag = VariableClipboardHelpers.parse(clipboard, GeneralConfig.variableClipboardMaxPayloadLength);
            // Parse client-side as well, so that errors are shown without a round trip
            getElement().setValue(VariableClipboardHelpers.deserialize(ValueDeseralizationContext.ofClient(), tag,
                    GeneralConfig.variableClipboardMaxPayloadLength));
        } catch (VariableClipboardHelpers.VariableClipboardException e) {
            getElement().setError(e.getErrorMessage());
            tag = new CompoundTag();
        }
        getContainer().onDirty();

        // The server validates the value again, the client is only trusted for showing a preview
        IntegratedDynamics._instance.getPacketHandler().sendToServer(
                new LogicProgrammerClipboardValueChangedPacket(tag));
    }

    @Override
    public void renderBg(GuiGraphics guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

        IValue value = getElement().getValue();
        if (value != null) {
            // Centered in the space right of the paste button
            int x = guiLeft + getX() + 40 + (getWidth() - 44) / 2;
            int y = guiTop + getY() + 11;
            RenderHelpers.drawScaledCenteredString(guiGraphics.pose(), guiGraphics.bufferSource(), fontRenderer,
                    value.getType().toCompactString(value).getString(),
                    x, y, getWidth() - 44, Helpers.RGBToInt(20, 20, 20), false, Font.DisplayMode.NORMAL);
        }
    }

}
