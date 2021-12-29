package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.Container;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
class OperatorLPElementRenderPattern extends RenderPattern<OperatorLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
        implements IRenderPatternValueTypeTooltip {

    private boolean renderTooltip = true;

    public OperatorLPElementRenderPattern(OperatorLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                          ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    public void drawGuiContainerForegroundLayer(PoseStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();
        IOperator operator = element.getOperator();

        // Input type tooltips
        IValueType[] valueTypes = operator.getInputTypes();
        for (int i = 0; i < valueTypes.length; i++) {
            IValueType valueType = valueTypes[i];
            Container temporaryInputSlots = container.getTemporaryInputSlots();
            if (temporaryInputSlots.getItem(i).isEmpty()) {
                Pair<Integer, Integer> slotPosition = configRenderPattern.getSlotPositions()[i];
                if (gui.isHovering(getX() + slotPosition.getLeft(), getY() + slotPosition.getRight(),
                        ContainerScreenLogicProgrammerBase.BOX_HEIGHT, ContainerScreenLogicProgrammerBase.BOX_HEIGHT, mouseX, mouseY)) {
                    gui.drawTooltip(getValueTypeTooltip(valueType), matrixStack, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

        // Output type tooltip
        this.drawTooltipForeground(gui, matrixStack, container, guiLeft, guiTop, mouseX, mouseY, operator.getOutputType());
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
