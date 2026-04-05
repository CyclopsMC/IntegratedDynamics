package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.infobook.ScreenInfoBook;
import org.cyclops.cyclopscore.infobook.pageelement.SectionAppendixClient;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * @author rubensworks
 */
public class OperatorAppendixClient extends SectionAppendixClient<OperatorAppendix> {
    protected OperatorAppendixClient(OperatorAppendix sectionAppendix) {
        super(sectionAppendix);
    }

    @Override
    protected void drawElement(ScreenInfoBook gui, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        OperatorAppendix section = getSectionAppendix();
        IOperator operator = section.getOperator();

        int yOffset = 5;
        gui.drawOuterBorder(guiGraphics, x - 1, y - 1 - yOffset, section.getWidth() + 2, section.getHeight() + 2, 0.5F, 0.5F, 0.5F, 0.4f);
        gui.drawTextBanner(guiGraphics, x + width / 2, y - 2 - yOffset);
        gui.drawScaledCenteredString(guiGraphics, IModHelpers.get().getL10NHelpers().localize("operator.integrateddynamics"), x, y - 2 - yOffset, width, 0.9f, gui.getBannerWidth() - 6, IModHelpers.get().getBaseHelpers().RGBAToInt(30, 20, 120, 255));

        // Base information
        String operatorName = IModHelpers.get().getL10NHelpers().localize(operator.getTranslationKey());
        gui.drawScaledCenteredString(guiGraphics, IModHelpers.get().getL10NHelpers().localize(operatorName) + " (" + operator.getSymbol() + ")", x, y + 8, width, 1f, gui.getBannerWidth(), ARGB.opaque(0));

        // Input/output types
        IValueType[] inputTypes = operator.getInputTypes();
        int offsetY = 14;
        for(int i = 0; i < inputTypes.length; i++) {
            guiGraphics.text(gui.getFont(), IModHelpers.get().getL10NHelpers().localize(L10NValues.GUI_INPUT, (i + 1) + ": "
                    + inputTypes[i].getDisplayColorFormat() + IModHelpers.get().getL10NHelpers().localize(inputTypes[i].getTranslationKey())), x, y + offsetY, ARGB.opaque(0), false);
            offsetY += 8;
        }
        String outputTypeName = IModHelpers.get().getL10NHelpers().localize(operator.getOutputType().getTranslationKey());
        guiGraphics.text(gui.getFont(), IModHelpers.get().getL10NHelpers().localize(L10NValues.GUI_OUTPUT,
                operator.getOutputType().getDisplayColorFormat() + outputTypeName), x, y + offsetY, ARGB.opaque(0), false);

        // Global/local names
        offsetY += 8;
        gui.drawScaledCenteredString(guiGraphics, IModHelpers.get().getL10NHelpers().localize(L10NValues.GUI_OPERATOR_GLOBALNAME,
                operator.getGlobalInteractName()), x, y + offsetY + 6, width, 1f, gui.getBannerWidth(), ARGB.opaque(0));
        offsetY += 8;
        if (operator.getInputTypes().length > 0) {
            String scopedTypeName = IModHelpers.get().getL10NHelpers().localize(operator.getInputTypes()[0].getTranslationKey());
            gui.drawScaledCenteredString(guiGraphics, IModHelpers.get().getL10NHelpers().localize(L10NValues.GUI_OPERATOR_LOCALNAME,
                    operator.getInputTypes()[0].getDisplayColorFormat() + scopedTypeName + "." + operator.getScopedInteractName()), x, y + offsetY + 6, width, 1f, gui.getBannerWidth(), ARGB.opaque(0));
        }
    }

    @Override
    protected void postDrawElement(ScreenInfoBook gui, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        if(mx >= x && my >= y && mx <= x + getSectionAppendix().getWidth() && my <= y + gui.getFont().lineHeight ) {
            List<Component> lines = Lists.newArrayList();
            getSectionAppendix().getOperator().loadTooltip(lines::add, true);
            guiGraphics.setComponentTooltipForNextFrame(gui.getFont(), lines, mx, my);
        }
    }
}
