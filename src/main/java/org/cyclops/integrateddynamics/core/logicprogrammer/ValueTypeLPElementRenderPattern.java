package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeStringRenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public class ValueTypeLPElementRenderPattern extends GuiElementValueTypeStringRenderPattern<RenderPattern, GuiLogicProgrammerBase, ContainerLogicProgrammerBase>
        implements IRenderPatternValueTypeTooltip {

    private boolean renderTooltip = true;

    public ValueTypeLPElementRenderPattern(ValueTypeLPElementBase element, int baseX, int baseY, int maxWidth, int maxHeight,
                                           GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element.getInnerGuiElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        this.drawTooltipForeground(gui, container, guiLeft, guiTop, mouseX, mouseY, element.getValueType());
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
