package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTypeChecker;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.OperatorNames;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionPanelRendering.Region;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionPanelRendering.COLOR_TEXT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionPanelRendering.ELLIPSIS;
import static org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionPanelRendering.drawOutline;

/**
 * The tabs above the slots for the operators from the root of an expression to the one in the slots.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public final class ExpressionBreadcrumb {

    private static final int COLOR_TAB = FastColor.ARGB32.color(216, 220, 224);
    private static final int COLOR_TAB_CURRENT = FastColor.ARGB32.color(255, 255, 255);
    private static final int COLOR_TAB_OUTLINE = FastColor.ARGB32.color(139, 139, 139);
    private static final int COLOR_LINK = FastColor.ARGB32.color(32, 80, 160);
    private static final int SEPARATOR = 8;
    private static final int HEIGHT = 12;

    private ExpressionBreadcrumb() {
    }

    /**
     * Draw the tabs, if an operator below the root is in the slots.
     * @param navigate Opens the operator at a path.
     * @param regions Where the regions to click are added.
     */
    public static void render(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor,
                              ExpressionTypeChecker.Result result, Consumer<int[]> navigate, List<Region> regions) {
        int[] editPath = editor.getEditPath();
        if (editPath.length == 0) {
            return;
        }
        int x = ContainerLogicProgrammerBase.BASE_X + 2;
        int y = ContainerLogicProgrammerBase.BASE_Y + 2;
        List<String> names = new ArrayList<>();
        for (int i = 0; i < editor.getOperatorsAlongPath(editPath).size(); i++) {
            int[] tabPath = Arrays.copyOf(editPath, i);
            names.add(OperatorNames.getNodeName((ExpressionNode.Operator) ExpressionTrees.get(editor.getExpression().root(), tabPath), tabPath, result));
        }
        List<Tab> tabs = layout(font, names, ContainerLogicProgrammerBase.MAX_WIDTH - 4);
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            if (i > 0) {
                guiGraphics.drawString(font, "›", guiLeft + x + 1, guiTop + y + 2, COLOR_TEXT, false);
                x += SEPARATOR;
            }
            boolean current = i == tabs.size() - 1;
            guiGraphics.fill(guiLeft + x, guiTop + y, guiLeft + x + tab.width(), guiTop + y + HEIGHT, current ? COLOR_TAB_CURRENT : COLOR_TAB);
            drawOutline(guiGraphics, guiLeft + x, guiTop + y, tab.width(), HEIGHT, current ? COLOR_TEXT : COLOR_TAB_OUTLINE);
            guiGraphics.drawString(font, tab.text(), guiLeft + x + 3, guiTop + y + 2, current ? COLOR_TEXT : COLOR_LINK, false);
            if (!current) {
                int[] tabPath = Arrays.copyOf(editPath, tab.depth());
                // Collapsed tabs tell which operators they stand for
                Component tooltip = tab.collapsed()
                        ? Component.literal(String.join(" › ", names.subList(1, tab.depth() + 1)))
                        : Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_BREADCRUMB);
                regions.add(new Region(x, y, tab.width(), HEIGHT, button -> navigate.accept(tabPath), tooltip));
            }
            x += tab.width();
        }
    }

    /**
     * Lay out the tabs in the given width, as paths are shortened:
     * the tabs in the middle that do not fit are collapsed into one, and the names of the others are cut off if they still do not fit.
     * @param font The font.
     * @param names The names of the operators from the root to the edited one.
     * @param available The width.
     * @return The tabs to draw, where a collapsed one leads to the last operator it stands for.
     */
    public static List<Tab> layout(Font font, List<String> names, int available) {
        int count = names.size();
        // Show the root, then the last tabs from this one on, with a collapsed tab in between if any are left out
        int shownFrom = 1;
        while (shownFrom < count - 1 && getWidth(font, names, shownFrom, Integer.MAX_VALUE) > available) {
            shownFrom++;
        }
        int maxWidth = names.stream().mapToInt(font::width).max().orElse(0);
        while (maxWidth > font.width(ELLIPSIS) && getWidth(font, names, shownFrom, maxWidth) > available) {
            maxWidth--;
        }
        List<Tab> tabs = new ArrayList<>();
        tabs.add(new Tab(font, cutOff(font, names.get(0), maxWidth), 0, false));
        if (shownFrom > 1) {
            tabs.add(new Tab(font, ELLIPSIS, shownFrom - 1, true));
        }
        for (int i = shownFrom; i < count; i++) {
            tabs.add(new Tab(font, cutOff(font, names.get(i), maxWidth), i, false));
        }
        return tabs;
    }

    private static int getWidth(Font font, List<String> names, int shownFrom, int maxWidth) {
        int width = Math.min(font.width(names.get(0)), maxWidth) + 6;
        if (shownFrom > 1) {
            width += SEPARATOR + font.width(ELLIPSIS) + 6;
        }
        for (int i = shownFrom; i < names.size(); i++) {
            width += SEPARATOR + Math.min(font.width(names.get(i)), maxWidth) + 6;
        }
        return width;
    }

    private static String cutOff(Font font, String text, int width) {
        return font.width(text) <= width ? text : font.plainSubstrByWidth(text, width - font.width(ELLIPSIS)) + ELLIPSIS;
    }

    /**
     * A tab.
     * @param text Its text.
     * @param depth The depth of the operator it leads to.
     * @param collapsed If it stands for several operators that are left out.
     * @param width Its width.
     */
    public static record Tab(String text, int depth, boolean collapsed, int width) {
        Tab(Font font, String text, int depth, boolean collapsed) {
            this(text, depth, collapsed, font.width(text) + 6);
        }
    }

}
