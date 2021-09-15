package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonCheckbox;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeStringRenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeBooleanValueChangedPacket;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeStringValueChangedPacket;

import java.awt.*;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ValueTypeBooleanLPElementRenderPattern extends RenderPattern<ValueTypeBooleanLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
        implements IRenderPatternValueTypeTooltip {

    private boolean renderTooltip = true;
    @Getter
    private ButtonCheckbox checkbox = null;

    public ValueTypeBooleanLPElementRenderPattern(ValueTypeBooleanLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                                  ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);

        this.checkbox = new ButtonCheckbox(guiLeft + getX(), guiTop + getY(), getElement().getRenderPattern().getWidth(), getElement().getRenderPattern().getHeight(),
                new TranslationTextComponent(this.getElement().getValueType().getTranslationKey()), (entry) -> this.onChecked(this.checkbox.isChecked()));
        this.checkbox.setChecked(this.getElement().isInputBoolean());
    }

    @Override
    public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        this.checkbox.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        this.drawTooltipForeground(gui, container, guiLeft, guiTop, mouseX, mouseY, element.getValueType());
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
