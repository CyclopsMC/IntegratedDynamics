package org.cyclops.integrateddynamics.core.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;

import java.util.List;

/**
 * A component for displaying errors.
 * @author rubensworks
 */
public class DisplayErrorsComponent {

    public void drawForeground(List<L10NHelpers.UnlocalizedString> errors, int errorX, int errorY, int mouseX, int mouseY, GuiContainerExtended gui, int guiLeft, int guiTop) {
        if(!errors.isEmpty()) {
            if(gui.isPointInRegion(errorX, errorY, Images.ERROR.getSheetWidth(), Images.ERROR.getSheetHeight(), mouseX, mouseY)) {
                List<String> lines = Lists.newLinkedList();
                for(L10NHelpers.UnlocalizedString error : errors) {
                    lines.addAll(StringHelpers.splitLines(error.localize(), L10NHelpers.MAX_TOOLTIP_LINE_LENGTH,
                            TextFormatting.RED.toString()));
                }
                gui.drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

    public void drawBackground(List<L10NHelpers.UnlocalizedString> errors, int errorX, int errorY, int okX, int okY, GuiContainerExtended gui, int guiLeft, int guiTop, boolean okCondition) {
        // Render error symbol
        if(!errors.isEmpty()) {
            Images.ERROR.draw(gui, guiLeft + errorX, guiTop + errorY);
        } else if(okCondition) {
            Images.OK.draw(gui, guiLeft + okX, guiTop + okY);
        }
    }

}
