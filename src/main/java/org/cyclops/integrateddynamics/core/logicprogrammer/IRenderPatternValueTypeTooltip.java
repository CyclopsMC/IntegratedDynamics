package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import java.util.List;
import java.util.stream.Stream;

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

    public default void drawTooltipForeground(ContainerScreenLogicProgrammerBase gui, GuiGraphics guiGraphics, ContainerLogicProgrammerBase container, int guiLeft, int guiTop, int mouseX, int mouseY, IValueType valueType) {
        if (isRenderTooltip()) {
            // Output type tooltip
            if (!container.hasWriteItemInSlot()) {
                if (gui.isHovering(ContainerLogicProgrammerBase.OUTPUT_X, ContainerLogicProgrammerBase.OUTPUT_Y,
                        ContainerScreenLogicProgrammerBase.BOX_HEIGHT, ContainerScreenLogicProgrammerBase.BOX_HEIGHT, mouseX, mouseY)
                        && Minecraft.getInstance().player.containerMenu.getCarried().isEmpty()) {
                    List<Component> tooltips = Lists.newArrayList(Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_TOOLTIP_WRITESLOT_CREATE));
                    tooltips = Stream.concat(tooltips.stream(), getValueTypeTooltip(valueType).stream()).toList();
                    gui.drawTooltip(tooltips, guiGraphics.pose(), mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }
    }

}
