package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonCheckbox;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.core.logicprogrammer.IRenderPatternValueTypeTooltip;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPattern;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeBooleanValueChangedPacket;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class GuiElementValueTypeBooleanRenderPattern<S extends ISubGuiBox, G extends Screen, C extends AbstractContainerMenu> extends RenderPattern<GuiElementValueTypeBoolean<G, C>, G, C>
        implements IRenderPatternValueTypeTooltip {

    @Getter
    protected final GuiElementValueTypeBoolean<G, C> element;
    private boolean renderTooltip = true;
    @Getter
    private ButtonCheckbox checkbox = null;

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
    public void renderBg(GuiGraphics guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        this.checkbox.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return this.checkbox.mouseClicked(mouseX, mouseY, mouseButton) || super.mouseClicked(mouseX, mouseY, mouseButton);
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
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerValueTypeBooleanValueChangedPacket(checked));
    }
}
