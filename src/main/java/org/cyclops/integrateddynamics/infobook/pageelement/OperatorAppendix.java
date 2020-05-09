package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.infobook.GuiInfoBook;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoSection;
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
        return 30 + (operator.getInputTypes().length) * 8;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void drawElement(GuiInfoBook gui, int x, int y, int width, int height, int page, int mx, int my) {
        int yOffset = 5;
        gui.drawOuterBorder(x - 1, y - 1 - yOffset, getWidth() + 2, getHeight() + 2, 0.5F, 0.5F, 0.5F, 0.4f);
        gui.drawTextBanner(x + width / 2, y - 2 - yOffset);
        gui.drawScaledCenteredString(L10NHelpers.localize("operator.operators.integrateddynamics.name"), x, y - 2 - yOffset, width, 0.9f, gui.getBannerWidth() - 6, Helpers.RGBToInt(120, 20, 30));

        // Base information
        String operatorName = L10NHelpers.localize(operator.getTranslationKey());
        gui.drawScaledCenteredString(L10NHelpers.localize(operatorName) + " (" + operator.getSymbol() + ")", x, y + 8, width, 1f, gui.getBannerWidth(), 0);
        boolean wasUnicode = gui.getFontRenderer().getUnicodeFlag();
        gui.getFontRenderer().setUnicodeFlag(true);

        // Input/output types
        IValueType[] inputTypes = operator.getInputTypes();
        int offsetY = 14;
        for(int i = 0; i < inputTypes.length; i++) {
            gui.getFontRenderer().drawString(L10NHelpers.localize(L10NValues.GUI_INPUT, (i + 1) + ": "
                    + inputTypes[i].getDisplayColorFormat() + L10NHelpers.localize(inputTypes[i].getTranslationKey())), x, y + offsetY, 0);
            offsetY += 8;
        }
        String outputTypeName = L10NHelpers.localize(operator.getOutputType().getTranslationKey());
        gui.getFontRenderer().drawString(L10NHelpers.localize(L10NValues.GUI_OUTPUT,
                operator.getOutputType().getDisplayColorFormat() + outputTypeName), x, y + offsetY, 0);

        gui.getFontRenderer().setUnicodeFlag(wasUnicode);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void postDrawElement(GuiInfoBook gui, int x, int y, int width, int height, int page, int mx, int my) {
        GlStateManager.pushMatrix();
        if(mx >= x && my >= y && mx <= x + getWidth() && my <= y + gui.getFontRenderer().FONT_HEIGHT ) {
            List<String> lines = Lists.newArrayList();
            operator.loadTooltip(lines, true);
            gui.drawHoveringText(lines, mx, my);
        }
        GlStateManager.popMatrix();

        GlStateManager.disableLighting();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void preBakeElement(InfoSection infoSection) {

    }

    @Override
    public void bakeElement(InfoSection infoSection) {

    }

}
