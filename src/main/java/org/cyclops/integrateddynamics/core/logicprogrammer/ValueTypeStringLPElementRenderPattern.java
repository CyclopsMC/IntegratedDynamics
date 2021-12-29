package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeStringRenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ValueTypeStringLPElementRenderPattern extends GuiElementValueTypeStringRenderPattern<RenderPattern, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
        implements IRenderPatternValueTypeTooltip {

    private boolean renderTooltip = true;

    public ValueTypeStringLPElementRenderPattern(ValueTypeStringLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                                 ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element.getInnerGuiElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    public void drawGuiContainerForegroundLayer(PoseStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        this.drawTooltipForeground(gui, matrixStack, container, guiLeft, guiTop, mouseX, mouseY, element.getValueType());
    }

    @Override
    public boolean isRenderTooltip() {
        return this.renderTooltip;
    }

    @Override
    public void setRenderTooltip(boolean renderTooltip) {
        this.renderTooltip = renderTooltip;
    }
}
