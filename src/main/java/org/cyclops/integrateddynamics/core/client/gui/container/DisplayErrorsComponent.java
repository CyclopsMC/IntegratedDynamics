package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A component for displaying errors.
 * @author rubensworks
 */
public class DisplayErrorsComponent {

    public void drawForeground(PoseStack matrixStack, @Nullable List<MutableComponent> errors, int errorX, int errorY, int mouseX, int mouseY, ContainerScreenExtended<?> gui, int guiLeft, int guiTop) {
        if(errors != null && !errors.isEmpty()) {
            if(gui.isHovering(errorX, errorY, Images.ERROR.getSheetWidth(), Images.ERROR.getSheetHeight(), mouseX, mouseY)) {
                List<Component> lines = Lists.newLinkedList();
                for(Component error : errors) {
                    lines.addAll(StringHelpers.splitLines(error.getString(), L10NHelpers.MAX_TOOLTIP_LINE_LENGTH,
                            ChatFormatting.RED.toString())
                            .stream()
                            .map(Component::literal)
                            .collect(Collectors.toList()));
                }
                gui.drawTooltip(lines, matrixStack, mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

    public void drawBackground(PoseStack matrixStack, @Nullable List<MutableComponent> errors, int errorX, int errorY, int okX, int okY, ContainerScreenExtended<?> gui, int guiLeft, int guiTop, boolean okCondition) {
        // Render error symbol
        if(errors != null && !errors.isEmpty()) {
            Images.ERROR.draw(gui, matrixStack, guiLeft + errorX, guiTop + errorY);
        } else if(okCondition) {
            Images.OK.draw(gui, matrixStack, guiLeft + okX, guiTop + okY);
        }
    }

}
