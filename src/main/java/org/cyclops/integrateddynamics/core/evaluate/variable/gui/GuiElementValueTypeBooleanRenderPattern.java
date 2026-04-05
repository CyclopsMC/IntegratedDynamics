package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonCheckbox;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.core.logicprogrammer.IRenderPatternValueTypeTooltip;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.RenderPattern;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeBooleanValueChangedPacket;

/**
 * @author rubensworks
 */
public class GuiElementValueTypeBooleanRenderPattern<S extends ISubGuiBox, G extends Screen, C extends AbstractContainerMenu> extends RenderPattern<GuiElementValueTypeBoolean<G, C>, G, C>
        implements IRenderPatternValueTypeTooltip {

    protected final GuiElementValueTypeBoolean<G, C> element;
    private boolean renderTooltip = true;
    private ButtonCheckbox checkbox = null;

    public GuiElementValueTypeBoolean<G, C> getElement() {
        return element;
    }

    public ButtonCheckbox getCheckbox() {
        return checkbox;
    }

    public GuiElementValueTypeBooleanRenderPattern(GuiElementValueTypeBoolean<G, C> element, int baseX, int baseY, int maxWidth, int maxHeight,
                                                   G gui, C container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.element = element;
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);

        this.checkbox = new ButtonCheckbox(guiLeft + getX(), guiTop + getY(), getElement().getRenderPattern().getWidth(), getElement().getRenderPattern().getHeight(),
                Component.translatable(this.getElement().getValueType().getTranslationKey()), (entry) -> this.onChecked(this.checkbox.isChecked()));

        boolean value = element.getInputBoolean();
        this.checkbox.setChecked(value);
    }

    @Override
    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        this.checkbox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        return this.checkbox.mouseClicked(evt, isDoubleClick) || super.mouseClicked(evt, isDoubleClick);
    }

    @Override
    public boolean isRenderTooltip() {
        return this.renderTooltip;
    }

    @Override
    public void setRenderTooltip(boolean renderTooltip) {
        this.renderTooltip = renderTooltip;
    }

    protected void onChecked(boolean checked) {
        if (container instanceof IDirtyMarkListener) {
            ((IDirtyMarkListener) container).onDirty();
        }
        this.getElement().setInputBoolean(checked);
        sendValueToServer();
    }

    @Override
    public void sendValueToServer() {
        super.sendValueToServer();
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerValueTypeBooleanValueChangedPacket(this.getElement().getInputBoolean()));
    }
}
