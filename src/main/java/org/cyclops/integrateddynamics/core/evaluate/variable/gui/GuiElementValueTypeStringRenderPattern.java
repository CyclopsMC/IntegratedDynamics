package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPattern;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeStringValueChangedPacket;

/**
 * A render pattern for value types that can be read from and written to strings.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class GuiElementValueTypeStringRenderPattern<S extends ISubGuiBox, G extends Screen, C extends AbstractContainerMenu> extends RenderPattern<GuiElementValueTypeString<G, C>, G, C> {

    @Getter
    protected final GuiElementValueTypeString<G, C> element;
    @Getter
    private WidgetTextFieldExtended textField = null;

    public GuiElementValueTypeStringRenderPattern(GuiElementValueTypeString<G, C> element, int baseX, int baseY, int maxWidth, int maxHeight,
                                                  G gui, C container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.element = element;
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        Font fontRenderer = Minecraft.getInstance().font;
        int searchWidth = getElement().getRenderPattern().getWidth() - 28;
        int searchX = getX() + 14;
        int searchY = getY() + 6;
        this.textField = new WidgetTextFieldExtended(fontRenderer, guiLeft + searchX, guiTop + searchY, searchWidth,
                fontRenderer.lineHeight + 3, Component.translatable(this.getElement().getValueType().getTranslationKey()), true);
        this.textField.setMaxLength(512);
        this.textField.setBordered(false);
        this.textField.setVisible(true);
        this.textField.setTextColor(16777215);
        this.textField.setCanLoseFocus(true);
        String value = element.getInputString();
        if (value == null) {
            value = element.getDefaultInputString();
        }
        this.textField.setValue(value);
        element.setInputString(textField.getValue());
        this.textField.setWidth(searchWidth);
        this.textField.setX(guiLeft + (searchX + searchWidth) - this.textField.getWidth());
    }

    @Override
    public void renderBg(GuiGraphics guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        // Textbox
        textField.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (textField.isFocused()) {
            if (textField.charTyped(typedChar, keyCode)) {
                onTyped();
                return true;
            }
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (textField.isFocused()) {
            textField.keyPressed(typedChar, keyCode, modifiers);
            onTyped();
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    private void onTyped() {
        element.setInputString(textField.getValue());
        if (container instanceof IDirtyMarkListener) {
            ((IDirtyMarkListener) container).onDirty();
        }
        IntegratedDynamics._instance.getPacketHandler().sendToServer(
                new LogicProgrammerValueTypeStringValueChangedPacket(element.getInputString()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return textField.mouseClicked(mouseX, mouseY, mouseButton) || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
