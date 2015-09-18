package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammer;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeSubGuiRenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

import java.util.List;

/**
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public class ValueTypeElementSubGuiRenderPattern extends ValueTypeSubGuiRenderPattern<GuiLogicProgrammer, ContainerLogicProgrammer> {

    public ValueTypeElementSubGuiRenderPattern(ValueTypeElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                               GuiLogicProgrammer gui, ContainerLogicProgrammer container) {
        super(element.getInnerGuiElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        IValueType valueType = element.getValueType();

        // Output type tooltip
        if (!container.hasWriteItemInSlot()) {
            if (gui.isPointInRegion(ContainerLogicProgrammer.OUTPUT_X, ContainerLogicProgrammer.OUTPUT_Y,
                    GuiLogicProgrammer.BOX_HEIGHT, GuiLogicProgrammer.BOX_HEIGHT, mouseX, mouseY)) {
                List<String> lines = Lists.newLinkedList();
                lines.add(valueType.getDisplayColorFormat() + L10NHelpers.localize(valueType.getUnlocalizedName()));
                gui.drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

}
