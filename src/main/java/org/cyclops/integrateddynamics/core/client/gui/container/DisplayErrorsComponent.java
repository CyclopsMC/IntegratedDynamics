package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A component for displaying errors.
 * @author rubensworks
 */
public class DisplayErrorsComponent {

    public void drawForeground(@Nullable List<ITextComponent> errors, int errorX, int errorY, int mouseX, int mouseY, ContainerScreenExtended<?> gui, int guiLeft, int guiTop) {
        if(errors != null && !errors.isEmpty()) {
            if(gui.isPointInRegion(errorX, errorY, Images.ERROR.getSheetWidth(), Images.ERROR.getSheetHeight(), mouseX, mouseY)) {
                List<ITextComponent> lines = Lists.newLinkedList();
                for(ITextComponent error : errors) {
                    lines.addAll(StringHelpers.splitLines(error.getFormattedText(), L10NHelpers.MAX_TOOLTIP_LINE_LENGTH,
                            TextFormatting.RED.toString())
                            .stream()
                            .map(StringTextComponent::new)
                            .collect(Collectors.toList()));
                }
                gui.drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

    public void drawBackground(@Nullable List<ITextComponent> errors, int errorX, int errorY, int okX, int okY, ContainerScreenExtended<?> gui, int guiLeft, int guiTop, boolean okCondition) {
        // Render error symbol
        if(errors != null && !errors.isEmpty()) {
            Images.ERROR.draw(gui, guiLeft + errorX, guiTop + errorY);
        } else if(okCondition) {
            Images.OK.draw(gui, guiLeft + okX, guiTop + okY);
        }
    }

}
