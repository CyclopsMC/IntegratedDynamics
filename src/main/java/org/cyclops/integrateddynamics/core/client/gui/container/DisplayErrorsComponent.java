package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A component for displaying errors.
 * @author rubensworks
 */
public class DisplayErrorsComponent {

    public void drawForeground(GuiGraphicsExtractor guiGraphics, @Nullable List<Component> errors, int errorX, int errorY, int mouseX, int mouseY, ContainerScreenExtended<?> gui, int guiLeft, int guiTop) {
        if(errors != null && !errors.isEmpty()) {
            if(gui.isHovering(errorX, errorY, Images.ERROR.getSheetWidth(), Images.ERROR.getSheetHeight(), mouseX, mouseY)) {
                List<Component> lines = Lists.newLinkedList();
                for(Component error : errors) {
                    lines.addAll(StringHelpers.splitLines(error.getString(), IModHelpers.get().getL10NHelpers().getMaxTooltipLineLength(),
                            ChatFormatting.RED.toString())
                            .stream()
                            .map(Component::literal)
                            .collect(Collectors.toList()));
                }
                gui.drawTooltip(lines, guiGraphics, mouseX, mouseY);
            }
        }
    }

    public void drawBackground(GuiGraphicsExtractor guiGraphics, @Nullable List<Component> errors, int errorX, int errorY, int okX, int okY, ContainerScreenExtended<?> gui, int guiLeft, int guiTop, boolean okCondition) {
        // Render error symbol
        if(errors != null && !errors.isEmpty()) {
            Images.ERROR.draw(guiGraphics, guiLeft + errorX, guiTop + errorY);
        } else if(okCondition) {
            Images.OK.draw(guiGraphics, guiLeft + okX, guiTop + okY);
        }
    }

}
