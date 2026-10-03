package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.ValueEncodingHelpers;
import org.cyclops.integrateddynamics.core.logicprogrammer.DecodeLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerDecodeValueChangedPacket;

/**
 * Render pattern for the decode element, into which an encoded value is pasted.
 * @author rubensworks
 */
public class DecodeLPElementRenderPattern extends RenderPattern<DecodeLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private static final int COLOR_VALUE = ARGB.color(40, 40, 40);
    private static final int COLOR_HINT = ARGB.color(110, 110, 110);
    private static final int COLOR_ERROR = ARGB.color(200, 20, 20);

    private WidgetTextFieldExtended textField = null;

    public WidgetTextFieldExtended getTextField() {
        return textField;
    }

    public DecodeLPElementRenderPattern(DecodeLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                           ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);
        Font font = Minecraft.getInstance().font;
        this.textField = new WidgetTextFieldExtended(font, guiLeft + getX() + 6, guiTop + getY() + 6, getWidth() - 12,
                font.lineHeight + 3, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_DECODE), true);
        this.textField.setMaxLength(GeneralConfig.valueEncodingMaxLength);
        this.textField.setBordered(false);
        this.textField.setTextColor(ARGB.opaque(16777215));
        this.textField.setCanLoseFocus(true);
        this.textField.setValue(getElement().getInputString());
    }

    @Override
    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        textField.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        // Below the text field, show what the pasted value is, why it is invalid, or what to do
        int centerX = guiLeft + getX() + getWidth() / 2;
        int y = guiTop + getY() + 22;
        int width = getWidth() - 12;
        IValue value = getElement().getValue();
        if (value != null) {
            Component type = Component.translatable(value.getType().getTranslationKey());
            drawCentered(guiGraphics, fontRenderer, fontRenderer.plainSubstrByWidth(type.getString(), width),
                    centerX, y, ARGB.opaque(value.getType().getDisplayColor()));
            drawCentered(guiGraphics, fontRenderer, fontRenderer.plainSubstrByWidth(
                    value.getType().toCompactString(value).getString(), width), centerX, y + 10, COLOR_VALUE);
        } else if (getElement().getError() != null) {
            drawCenteredWrapped(guiGraphics, fontRenderer, getElement().getError(), centerX, y, width, COLOR_ERROR);
        } else {
            // A hint inside the text field would only show while it is not focused
            drawCenteredWrapped(guiGraphics, fontRenderer, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_DECODE_HINT),
                    centerX, y, width, COLOR_HINT);
        }
    }

    protected void drawCentered(GuiGraphicsExtractor guiGraphics, Font font, String text, int centerX, int y, int color) {
        guiGraphics.text(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    protected void drawCenteredWrapped(GuiGraphicsExtractor guiGraphics, Font font, Component text, int centerX, int y, int width, int color) {
        for (FormattedCharSequence line : font.split(text, width)) {
            guiGraphics.text(font, line, centerX - font.width(line) / 2, y, color, false);
            y += font.lineHeight;
        }
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if (textField.isFocused() && textField.charTyped(evt)) {
            onChanged();
            return true;
        }
        return super.charTyped(evt);
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (textField.isFocused()) {
            // Also handles pasting
            textField.keyPressed(evt);
            onChanged();
            return true;
        }
        return super.keyPressed(evt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        return textField.mouseClicked(evt, isDoubleClick) || super.mouseClicked(evt, isDoubleClick);
    }

    protected void onChanged() {
        String input = textField.getValue();
        if (input.equals(getElement().getInputString())) {
            return;
        }
        getElement().setInputString(input);

        CompoundTag tag = new CompoundTag();
        if (input.isBlank()) {
            getElement().clear();
        } else {
            try {
                tag = ValueEncodingHelpers.parse(input, GeneralConfig.valueEncodingMaxLength);
                // Validated client-side as well, so that errors are shown without a round trip
                getElement().setValue(ValueEncodingHelpers.decode(ValueDeseralizationContext.ofClient(), tag,
                        GeneralConfig.valueEncodingMaxLength));
            } catch (ValueEncodingHelpers.ValueEncodingException e) {
                getElement().setError(e.getErrorMessage());
                tag = new CompoundTag();
            }
        }
        getContainer().onDirty();

        // The server validates the value again, the client is only trusted for showing it
        IntegratedDynamics._instance.getPacketHandler().sendToServer(
                new LogicProgrammerDecodeValueChangedPacket(tag));
    }

}
