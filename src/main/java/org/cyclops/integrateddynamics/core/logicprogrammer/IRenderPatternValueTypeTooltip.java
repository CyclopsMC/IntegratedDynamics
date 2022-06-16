package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import java.util.List;

/**
 * @author rubensworks
 */
public interface IRenderPatternValueTypeTooltip {

    public default List<Component> getValueTypeTooltip(IValueType<?> valueType) {
        List<Component> lines = Lists.newLinkedList();
        lines.add(Component.translatable(valueType.getTranslationKey())
                .withStyle(valueType.getDisplayColorFormat()));
        return lines;
    }

    public abstract boolean isRenderTooltip();

    public abstract void setRenderTooltip(boolean renderTooltip);

    public default void drawTooltipForeground(ContainerScreenLogicProgrammerBase gui, PoseStack poseStack, ContainerLogicProgrammerBase container, int guiLeft, int guiTop, int mouseX, int mouseY, IValueType valueType) {
        if (isRenderTooltip()) {
            // Output type tooltip
            if (!container.hasWriteItemInSlot()) {
                if (gui.isHovering(ContainerLogicProgrammerBase.OUTPUT_X, ContainerLogicProgrammerBase.OUTPUT_Y,
                        ContainerScreenLogicProgrammerBase.BOX_HEIGHT, ContainerScreenLogicProgrammerBase.BOX_HEIGHT, mouseX, mouseY)) {
                    gui.drawTooltip(getValueTypeTooltip(valueType), poseStack, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }
    }

}
