package org.cyclops.integrateddynamics.core.logicprogrammer;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.ValueEncodingHelpers;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerDecodeValueChangedPacket;

/**
 * Render pattern for the decode element, into which an encoded value is pasted.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class DecodeLPElementRenderPattern extends RenderPattern<DecodeLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private static final int COLOR_VALUE = Helpers.RGBToInt(40, 40, 40);
    private static final int COLOR_HINT = Helpers.RGBToInt(110, 110, 110);
    private static final int COLOR_ERROR = Helpers.RGBToInt(200, 20, 20);

    @Getter
    private WidgetTextFieldExtended textField = null;

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
        this.textField.setTextColor(16777215);
        this.textField.setCanLoseFocus(true);
        this.textField.setValue(getElement().getInputString());
    }

    @Override
    public void renderBg(GuiGraphics guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        textField.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Below the text field, show what the pasted value is, why it is invalid, or what to do
        int centerX = guiLeft + getX() + getWidth() / 2;
        int y = guiTop + getY() + 22;
        int width = getWidth() - 12;
        IValue value = getElement().getValue();
        if (value != null) {
            Component type = Component.translatable(value.getType().getTranslationKey());
            drawCentered(guiGraphics, fontRenderer, fontRenderer.plainSubstrByWidth(type.getString(), width),
                    centerX, y, value.getType().getDisplayColor());
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

    protected void drawCentered(GuiGraphics guiGraphics, Font font, String text, int centerX, int y, int color) {
        guiGraphics.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    protected void drawCenteredWrapped(GuiGraphics guiGraphics, Font font, Component text, int centerX, int y, int width, int color) {
        for (FormattedCharSequence line : font.split(text, width)) {
            guiGraphics.drawString(font, line, centerX - font.width(line) / 2, y, color, false);
            y += font.lineHeight;
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (textField.isFocused() && textField.charTyped(typedChar, keyCode)) {
            onChanged();
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (textField.isFocused()) {
            // Also handles pasting
            textField.keyPressed(typedChar, keyCode, modifiers);
            onChanged();
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return textField.mouseClicked(mouseX, mouseY, mouseButton) || super.mouseClicked(mouseX, mouseY, mouseButton);
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
