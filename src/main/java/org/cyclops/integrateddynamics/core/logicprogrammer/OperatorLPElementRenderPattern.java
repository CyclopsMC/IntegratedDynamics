package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
class OperatorLPElementRenderPattern extends RenderPattern<OperatorLPElement, GuiLogicProgrammerBase, ContainerLogicProgrammerBase>
        implements IRenderPatternValueTypeTooltip {

    private boolean renderTooltip = true;

    public OperatorLPElementRenderPattern(OperatorLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                          GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();
        IOperator operator = element.getOperator();

        // Input type tooltips
        IValueType[] valueTypes = operator.getInputTypes();
        for (int i = 0; i < valueTypes.length; i++) {
            IValueType valueType = valueTypes[i];
            IInventory temporaryInputSlots = container.getTemporaryInputSlots();
            if (temporaryInputSlots.getStackInSlot(i).isEmpty()) {
                Pair<Integer, Integer> slotPosition = configRenderPattern.getSlotPositions()[i];
                if (gui.isPointInRegion(getX() + slotPosition.getLeft(), getY() + slotPosition.getRight(),
                        GuiLogicProgrammerBase.BOX_HEIGHT, GuiLogicProgrammerBase.BOX_HEIGHT, mouseX, mouseY)) {
                    gui.drawTooltip(getValueTypeTooltip(valueType), mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

        // Output type tooltip
        this.drawTooltipForeground(gui, container, guiLeft, guiTop, mouseX, mouseY, operator.getOutputType());
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
