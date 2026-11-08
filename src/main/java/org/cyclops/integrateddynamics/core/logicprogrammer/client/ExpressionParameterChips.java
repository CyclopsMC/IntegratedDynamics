package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionEditor;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.Region;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.List;

import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.CHIP_HEIGHT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_SELECTED;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_STRIP;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_TEXT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_TEXT_LIGHT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.ELLIPSIS;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.PANEL_WIDTH;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.PANEL_X;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.PARAMS_Y;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.drawOutline;

/**
 * The parameters of an expression as chips in the expression panel, which are clicked and then a slot, or dragged onto a slot.
 * While one is selected, its name and type can be changed in the row below the slots.
 * @author rubensworks
 */
public class ExpressionParameterChips {

    public static final int COLOR_PARAM = ARGB.color(232, 200, 64);
    private static final int COLOR_PARAM_ARMED = ARGB.color(255, 240, 160);
    private static final int COLOR_PARAM_OUTLINE = ARGB.color(128, 96, 16);
    // As many as 8 parameters with the longest names take, so the text keeps its room
    private static final int ROWS = 4;
    // In the tooltip of the parameters that do not fit
    private static final int MAX_LISTED = 32;
    private static final int DRAG_DISTANCE = 3;

    private final OperatorLPElementRenderPattern pattern;
    private final OperatorLPElement element;
    // A chip can also be dragged onto a slot, instead of clicking it and then the slot
    private int pressed = -1;
    private double pressX;
    private double pressY;
    private boolean dragging = false;

    public ExpressionParameterChips(OperatorLPElementRenderPattern pattern, OperatorLPElement element) {
        this.pattern = pattern;
        this.element = element;
    }

    /**
     * Lay out the chips in rows below the title, followed by the button to add one.
     * The rows are limited, and a chip tells how many parameters do not fit, which can be used in the text.
     * @param buttonWidth The width of the button to add a parameter.
     * @return Where the chips and the button go, relative to the gui.
     */
    public static ParameterChipLayout layout(Font font, List<ExpressionParameter> parameters, int buttonWidth) {
        // Long names are cut off, so they do not take a row each
        int[] widths = parameters.stream().mapToInt(parameter -> Math.min(font.width(parameter.name()) + 4, (PANEL_WIDTH - 2) / 2)).toArray();
        return ParameterChipLayout.layout(widths, hidden -> font.width(getMoreText(hidden)) + 4, buttonWidth,
                PANEL_X, PARAMS_Y, PANEL_WIDTH, CHIP_HEIGHT + 1, ROWS);
    }

    private static Component getMoreText(int hidden) {
        return Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_MORE, hidden);
    }

    private static Component getTooltip(ExpressionParameter parameter) {
        return Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_TOOLTIP, parameter.name(),
                Component.translatable(parameter.type().getTranslationKey()).withStyle(parameter.type().getDisplayColorFormat()));
    }

    /**
     * Draw the chips, with only their names, as their types are in their tooltips and in the type of the expression.
     * @param buttonWidth The width of the button to add a parameter.
     * @param regions Where the regions to click are added.
     * @return The y position below the chips and the button to add one, relative to the gui.
     */
    public int render(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor, int buttonWidth, List<Region> regions) {
        List<ExpressionParameter> parameters = editor.getExpression().parameters();
        ParameterChipLayout layout = layout(font, parameters, buttonWidth);
        List<int[]> chips = layout.chips();
        for (int i = 0; i < chips.size(); i++) {
            ExpressionParameter parameter = parameters.get(i);
            int x = chips.get(i)[0];
            int y = chips.get(i)[1];
            int width = chips.get(i)[2];
            String name = parameter.name();
            if (width < font.width(name) + 4) {
                name = font.plainSubstrByWidth(name, width - 4 - font.width(ELLIPSIS)) + ELLIPSIS;
            }
            boolean armed = element.getArmedParameter() == i;
            guiGraphics.fill(guiLeft + x, guiTop + y, guiLeft + x + width, guiTop + y + CHIP_HEIGHT, armed ? COLOR_PARAM_ARMED : COLOR_PARAM);
            drawOutline(guiGraphics, guiLeft + x, guiTop + y, width, CHIP_HEIGHT, armed ? COLOR_SELECTED : COLOR_PARAM_OUTLINE);
            guiGraphics.text(font, name, guiLeft + x + 2, guiTop + y + 2, COLOR_TEXT, false);
            int index = i;
            regions.add(new Region(x, y, width, CHIP_HEIGHT, button -> onClick(index, button), getTooltip(parameter)));
        }
        if (layout.more() != null) {
            renderMore(guiGraphics, guiLeft, guiTop, font, layout.more(), parameters.subList(chips.size(), parameters.size()), regions);
        }
        return layout.button()[1] + CHIP_HEIGHT + 2;
    }

    /**
     * Draw the chip of the parameters that do not fit, with their names and types in its tooltip.
     */
    private void renderMore(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, Font font, int[] chip, List<ExpressionParameter> hidden,
                            List<Region> regions) {
        guiGraphics.fill(guiLeft + chip[0], guiTop + chip[1], guiLeft + chip[0] + chip[2], guiTop + chip[1] + CHIP_HEIGHT, COLOR_STRIP);
        drawOutline(guiGraphics, guiLeft + chip[0], guiTop + chip[1], chip[2], CHIP_HEIGHT, COLOR_PARAM_OUTLINE);
        guiGraphics.text(font, getMoreText(hidden.size()), guiLeft + chip[0] + 2, guiTop + chip[1] + 2, COLOR_TEXT_LIGHT, false);
        MutableComponent names = Component.empty();
        for (int i = 0; i < Math.min(hidden.size(), MAX_LISTED); i++) {
            names.append(i > 0 ? Component.literal(", ") : Component.empty()).append(getTooltip(hidden.get(i)));
        }
        if (hidden.size() > MAX_LISTED) {
            names.append(", " + ELLIPSIS);
        }
        regions.add(new Region(chip[0], chip[1], chip[2], CHIP_HEIGHT, button -> {},
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_MORE_TOOLTIP, names)));
    }

    private void onClick(int index, int button) {
        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            pattern.armParameter(-1);
            pattern.sendAction(ExpressionEditor.actionRemoveParam(index));
        } else {
            pattern.armParameter(element.getArmedParameter() == index ? -1 : index);
            pattern.selectSlot(-1);
            pressed = index;
        }
    }

    /**
     * Forget a press, as a release can get lost, such as outside the window, and remember where this one is.
     */
    public void onMousePressed(double mouseX, double mouseY) {
        pressed = -1;
        dragging = false;
        pressX = mouseX;
        pressY = mouseY;
    }

    /**
     * @return If a chip that was pressed is being dragged.
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (pressed < 0 || button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (!dragging && Math.abs(mouseX - pressX) + Math.abs(mouseY - pressY) > DRAG_DISTANCE) {
            dragging = true;
        }
        return true;
    }

    /**
     * @return The parameter that was dragged and is released, or -1 if none was dragged.
     */
    public int mouseReleased() {
        int index = dragging ? pressed : -1;
        pressed = -1;
        dragging = false;
        return index;
    }

    public boolean isDragging() {
        return dragging;
    }

    /**
     * Draw the chip of the parameter that is being dragged under the mouse, above the items.
     */
    public void renderDragged(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor, int mouseX, int mouseY) {
        if (pressed >= editor.getExpression().parameters().size()) {
            return;
        }
        String name = editor.getExpression().parameters().get(pressed).name();
        int width = font.width(name) + 4;
        int x = mouseX - guiLeft - width / 2;
        int y = mouseY - guiTop - CHIP_HEIGHT / 2;
        // Above the items, which are drawn over everything else in the same stratum
        guiGraphics.nextStratum();
        guiGraphics.fill(x, y, x + width, y + CHIP_HEIGHT, COLOR_PARAM_ARMED);
        drawOutline(guiGraphics, x, y, width, CHIP_HEIGHT, COLOR_SELECTED);
        guiGraphics.text(font, name, x + 2, y + 2, COLOR_TEXT, false);
    }

}
