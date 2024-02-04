package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.ScreenInfoBook;
import org.cyclops.cyclopscore.infobook.pageelement.SectionAppendix;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Operator appendix.
 * @author rubensworks
 */
public class OperatorAppendix extends SectionAppendix {

    private final IOperator operator;

    public OperatorAppendix(IInfoBook infoBook, IOperator operator) {
        super(infoBook);
        this.operator = operator;
    }

    @Override
    protected int getOffsetY() {
        return 5;
    }

    @Override
    protected int getWidth() {
        return 100;
    }

    @Override
    protected int getHeight() {
        return 46 + (operator.getInputTypes().length) * 8;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawElement(ScreenInfoBook gui, GuiGraphics guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        int yOffset = 5;
        gui.drawOuterBorder(guiGraphics, x - 1, y - 1 - yOffset, getWidth() + 2, getHeight() + 2, 0.5F, 0.5F, 0.5F, 0.4f);
        gui.drawTextBanner(guiGraphics, x + width / 2, y - 2 - yOffset);
        gui.drawScaledCenteredString(guiGraphics, L10NHelpers.localize("operator.integrateddynamics"), x, y - 2 - yOffset, width, 0.9f, gui.getBannerWidth() - 6, Helpers.RGBToInt(120, 20, 30));

        // Base information
        String operatorName = L10NHelpers.localize(operator.getTranslationKey());
        gui.drawScaledCenteredString(guiGraphics, L10NHelpers.localize(operatorName) + " (" + operator.getSymbol() + ")", x, y + 8, width, 1f, gui.getBannerWidth(), 0);

        // Input/output types
        IValueType[] inputTypes = operator.getInputTypes();
        int offsetY = 14;
        for(int i = 0; i < inputTypes.length; i++) {
            gui.getFont().drawInBatch(L10NHelpers.localize(L10NValues.GUI_INPUT, (i + 1) + ": "
                    + inputTypes[i].getDisplayColorFormat() + L10NHelpers.localize(inputTypes[i].getTranslationKey())), x, y + offsetY, 0, false,
                    guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
            offsetY += 8;
        }
        String outputTypeName = L10NHelpers.localize(operator.getOutputType().getTranslationKey());
        gui.getFont().drawInBatch(L10NHelpers.localize(L10NValues.GUI_OUTPUT,
                operator.getOutputType().getDisplayColorFormat() + outputTypeName), x, y + offsetY, 0, false,
                guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);

        // Global/local names
        offsetY += 8;
        gui.drawScaledCenteredString(guiGraphics, L10NHelpers.localize(L10NValues.GUI_OPERATOR_GLOBALNAME,
                operator.getGlobalInteractName()), x, y + offsetY + 6, width, 1f, gui.getBannerWidth(), 0);
        offsetY += 8;
        if (operator.getInputTypes().length > 0) {
            String scopedTypeName = L10NHelpers.localize(operator.getInputTypes()[0].getTranslationKey());
            gui.drawScaledCenteredString(guiGraphics, L10NHelpers.localize(L10NValues.GUI_OPERATOR_LOCALNAME,
                    operator.getInputTypes()[0].getDisplayColorFormat() + scopedTypeName + "." + operator.getScopedInteractName()), x, y + offsetY + 6, width, 1f, gui.getBannerWidth(), 0);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void postDrawElement(ScreenInfoBook gui, GuiGraphics guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        if(mx >= x && my >= y && mx <= x + getWidth() && my <= y + gui.getFont().lineHeight ) {
            List<Component> lines = Lists.newArrayList();
            operator.loadTooltip(lines, true);
            guiGraphics.renderComponentTooltip(gui.getFont(), lines, mx, my);
        }

        //GlStateManager._disableLighting();

        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void preBakeElement(InfoSection infoSection) {

    }

    @Override
    public void bakeElement(InfoSection infoSection) {

    }

}
