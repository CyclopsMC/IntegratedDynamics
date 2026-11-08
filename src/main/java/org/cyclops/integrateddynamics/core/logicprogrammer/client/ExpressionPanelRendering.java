package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParseException;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTypeChecker;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.OperatorNames;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The layout, colors and drawing that the expression panel of an operator and the one without an operator share.
 * @author rubensworks
 */
public final class ExpressionPanelRendering {

    public static final int COLOR_TEXT = ARGB.color(64, 64, 64);
    public static final int COLOR_TEXT_LIGHT = ARGB.color(224, 224, 224);
    public static final int COLOR_TEXT_ERROR = ARGB.color(255, 96, 96);
    public static final int COLOR_ERROR = ARGB.color(224, 48, 48);
    public static final int COLOR_SELECTED = ARGB.color(48, 112, 255);
    // The dark box of the text and the footer, where the colors of types are readable
    public static final int COLOR_STRIP = ARGB.color(55, 55, 55);
    public static final int COLOR_STRIP_LINE = ARGB.color(85, 85, 85);
    public static final int COLOR_STRIP_HOVER = ARGB.color(90, 90, 90);
    public static final int COLOR_FOCUS = ARGB.color(160, 160, 160);
    public static final int COLOR_EMPTY = ARGB.color(160, 160, 160);
    public static final int COLOR_EDITED = ARGB.color(255, 255, 96);

    // The side panel, relative to the gui, with the same margin left and right, as the main texture covers the left of the panel
    public static final int PANEL_X = ContainerLogicProgrammerBase.EXPRESSION_PANEL_X + 7;
    public static final int PANEL_WIDTH = ContainerLogicProgrammerBase.EXPRESSION_PANEL_WIDTH - 13;
    public static final int TITLE_Y = ContainerLogicProgrammerBase.EXPRESSION_PANEL_Y + 6;
    // Buttons right of the title, with their icons in line with it
    public static final int BUTTONS_Y = TITLE_Y - 2;
    // The parameters, followed by the button to add one, with the text below them
    public static final int PARAMS_Y = TITLE_Y + 12;
    public static final int CHIP_HEIGHT = 12;
    public static final int LINE_HEIGHT = 9;
    public static final int CONTENT_BOTTOM = ContainerLogicProgrammerBase.EXPRESSION_PANEL_Y + ContainerLogicProgrammerBase.EXPRESSION_PANEL_HEIGHT - 6;
    // The type, or an error instead, below the text
    public static final int FOOTER_LINES = 3;
    public static final int FOOTER_HEIGHT = FOOTER_LINES * LINE_HEIGHT + 3;
    public static final int FOOTER_Y = CONTENT_BOTTOM - FOOTER_HEIGHT;
    public static final int TEXT_WIDTH = PANEL_WIDTH - 7;
    public static final String ELLIPSIS = "…";
    // How long typing pauses before valid text is applied
    public static final int APPLY_DELAY_MS = 750;

    private ExpressionPanelRendering() {
    }

    /**
     * Draw text centered in a box, at the largest of a few scales at which it fits, truncating it at the smallest one.
     */
    public static void drawFitted(GuiGraphicsExtractor guiGraphics, Font font, String text, int x, int y, int width, int height, int color) {
        float scale = 0.5F;
        for (float candidate : new float[]{1F, 0.75F}) {
            if (font.width(text) * candidate <= width) {
                scale = candidate;
                break;
            }
        }
        if (font.width(text) * scale > width) {
            while (text.length() > 1 && font.width(text + ELLIPSIS) * scale > width) {
                text = text.substring(0, text.length() - 1);
            }
            text = text + ELLIPSIS;
        }
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x + (width - font.width(text) * scale) / 2F, y + (height - 7 * scale) / 2F);
        guiGraphics.pose().scale(scale, scale);
        guiGraphics.text(font, text, 0, 0, color, false);
        guiGraphics.pose().popMatrix();
    }

    /**
     * @param background A background color.
     * @return Dark or light text, whichever can be read on it.
     */
    public static int getContrastingTextColor(int background) {
        int brightness = (ARGB.red(background) * 299 + ARGB.green(background) * 587
                + ARGB.blue(background) * 114) / 1000;
        return brightness > 128 ? COLOR_TEXT : COLOR_TEXT_LIGHT;
    }

    public static void drawOutline(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        guiGraphics.fill(x, y, x + 1, y + height, color);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    /**
     * Draw the box of the text: its outline while it is focused, and otherwise the line between it and the footer,
     * which is where the bottom of the outline goes.
     * @param x The left of the box.
     * @param y The top of the box.
     * @param bottom Where the footer starts.
     */
    public static void renderTextBox(GuiGraphicsExtractor guiGraphics, int x, int y, int bottom, int contentBottom, boolean focused) {
        guiGraphics.fill(x, y, x + PANEL_WIDTH, contentBottom, COLOR_STRIP);
        if (focused) {
            drawOutline(guiGraphics, x, y, PANEL_WIDTH, bottom - y, COLOR_FOCUS);
        } else {
            guiGraphics.fill(x, bottom - 1, x + PANEL_WIDTH, bottom, COLOR_STRIP_LINE);
        }
    }

    public static void renderTitle(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, Font font) {
        guiGraphics.text(font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TITLE),
                guiLeft + PANEL_X, guiTop + TITLE_Y, COLOR_TEXT, false);
    }

    /**
     * Draw the type of the expression in the footer, in the colors of its types as in tooltips,
     * or the first error instead, which keeps it from being written.
     * The check mark next to the operator name already tells when it can be written.
     */
    public static void renderStatus(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, Font font, Expression expression,
                                    ExpressionTypeChecker.Result result, List<Region> regions) {
        if (!result.errors().isEmpty()) {
            ExpressionTypeChecker.Error error = result.errors().get(0);
            Component status = error.message();
            if (error.slot() >= 0 && error.expected() != null && error.actual() != null) {
                String operatorName = ExpressionTrees.get(expression.root(), error.path()) instanceof ExpressionNode.Operator op
                        ? OperatorNames.getNodeName(op, error.path(), result) : "?";
                Component expected = Component.translatable(error.expected().getTranslationKey());
                Component actual = Component.translatable(error.actual().getTranslationKey());
                status = result.errors().size() == 1
                        ? Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_STATUS_ERRORS_SLOT, error.slot() + 1, operatorName, expected, actual)
                        : Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_STATUS_ERRORS_SLOT_MORE, error.slot() + 1, operatorName, expected, actual, result.errors().size());
            }
            renderFooter(guiGraphics, guiLeft, guiTop, font, status.copy().withStyle(ChatFormatting.RED),
                    error.message().copy().withStyle(ChatFormatting.RED), regions);
            return;
        }
        IValueType<?> outputType = result.outputType();
        Component type = expression.hasParameters()
                ? ValueTypeOperator.getSignature(ExpressionTypeChecker.getInputTypes(expression), outputType)
                : Component.translatable(outputType.getTranslationKey()).withStyle(outputType.getDisplayColorFormat());
        renderFooter(guiGraphics, guiLeft, guiTop, font, type, null, regions);
    }

    /**
     * Draw the message of an error in the text in the footer, without where it is, as the text marks it.
     */
    public static void renderTextError(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, Font font, ExpressionParseException error,
                                       List<Region> regions) {
        renderFooter(guiGraphics, guiLeft, guiTop, font, error.getComponent().copy().withStyle(ChatFormatting.RED), null, regions);
    }

    /**
     * Draw text centered in the footer, on the dark background of the expression where the colors of types are readable.
     * Text that does not fit ends in an ellipsis.
     * @param tooltip Shown when hovering the footer, defaults to the text if it does not fit.
     * @param regions Where the region for the tooltip is added.
     */
    public static void renderFooter(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, Font font, Component text, @Nullable Component tooltip,
                                    List<Region> regions) {
        guiGraphics.fill(guiLeft + PANEL_X, guiTop + FOOTER_Y, guiLeft + PANEL_X + PANEL_WIDTH, guiTop + CONTENT_BOTTOM, COLOR_STRIP);
        int width = PANEL_WIDTH - 4;
        List<FormattedText> lines = font.getSplitter().splitLines(text, width, Style.EMPTY);
        boolean truncated = lines.size() > FOOTER_LINES;
        int count = Math.min(lines.size(), FOOTER_LINES);
        int y = FOOTER_Y + 2 + (FOOTER_LINES - count) * LINE_HEIGHT / 2;
        for (int i = 0; i < count; i++) {
            FormattedText line = lines.get(i);
            if (truncated && i == count - 1) {
                line = FormattedText.composite(font.substrByWidth(line, width - font.width(ELLIPSIS)), FormattedText.of(ELLIPSIS));
            }
            guiGraphics.text(font, Language.getInstance().getVisualOrder(line), guiLeft + PANEL_X + 2, guiTop + y, COLOR_TEXT_LIGHT, false);
            y += LINE_HEIGHT;
        }
        if (tooltip != null || truncated) {
            regions.add(new Region(PANEL_X, FOOTER_Y, PANEL_WIDTH, FOOTER_HEIGHT, button -> {}, tooltip != null ? tooltip : text));
        }
    }

    /**
     * Draw the tooltip of the region under the mouse.
     */
    public static void renderRegionTooltips(ContainerScreenLogicProgrammerBase<?> gui, GuiGraphicsExtractor guiGraphics, List<Region> regions,
                                            int mouseX, int mouseY) {
        for (Region region : regions) {
            if (region.tooltip() != null && gui.isHovering(region.x(), region.y(), region.width(), region.height(), mouseX, mouseY)) {
                gui.drawTooltip(wrapTooltip(List.of(region.tooltip())), guiGraphics, mouseX, mouseY);
            }
        }
    }

    /**
     * @param lines Lines of a tooltip.
     * @return The lines, where long ones and ones with line breaks are split, in the color of the line.
     */
    private static ChatFormatting getFormatting(@Nullable TextColor color) {
        if (color != null) {
            // Named colors serialize to the lowercase name of their format
            for (ChatFormatting format : ChatFormatting.values()) {
                if (format.name().toLowerCase(Locale.ROOT).equals(color.serialize())) {
                    return format;
                }
            }
        }
        return ChatFormatting.WHITE;
    }

    public static List<Component> wrapTooltip(List<Component> lines) {
        List<Component> wrapped = new ArrayList<>();
        for (Component line : lines) {
            String text = line.getString();
            if (text.length() <= IModHelpers.get().getL10NHelpers().getMaxTooltipLineLength() && !text.contains("\n")) {
                wrapped.add(line);
            } else {
                ChatFormatting format = getFormatting(line.getStyle().getColor());
                for (String paragraph : text.split("\n")) {
                    for (String part : StringHelpers.splitLines(paragraph, IModHelpers.get().getL10NHelpers().getMaxTooltipLineLength(), format.toString())) {
                        wrapped.add(Component.literal(part));
                    }
                }
            }
        }
        return wrapped;
    }

    public static interface ClickAction {
        public void onClick(int mouseButton);
    }

    /**
     * A part of the panel that can be clicked or has a tooltip, relative to the gui.
     */
    public static record Region(int x, int y, int width, int height, ClickAction action, @Nullable Component tooltip) {
        public boolean contains(double x, double y) {
            return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height;
        }
    }

}
