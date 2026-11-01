package org.cyclops.integrateddynamics.core.logicprogrammer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * The places of the parameter chips in the expression panel: in rows, followed by the button to add a parameter.
 * The rows are limited, so the text below keeps its room. The chips that do not fit are left out,
 * and a chip in their place tells how many there are.
 * @param chips The x, y and width of the chip of each shown parameter, which are the first ones.
 * @param more The x, y and width of the chip of the parameters that are left out, or null if all are shown.
 * @param button The x, y and width of the button to add a parameter.
 * @author rubensworks
 */
public record ParameterChipLayout(List<int[]> chips, @Nullable int[] more, int[] button) {

    /**
     * @param chipWidths The width of the chip of each parameter.
     * @param moreWidth The width of the chip of the parameters that are left out, by how many there are.
     * @param buttonWidth The width of the button to add a parameter.
     * @param x The left of the rows.
     * @param y The top of the first row.
     * @param width The width of the rows.
     * @param rowHeight The distance between the tops of two rows.
     * @param rows The most rows.
     * @return The layout.
     */
    public static ParameterChipLayout layout(int[] chipWidths, IntUnaryOperator moreWidth, int buttonWidth,
                                             int x, int y, int width, int rowHeight, int rows) {
        Rows placed = new Rows(x, y, width, rowHeight, rows);
        List<int[]> chips = new ArrayList<>();
        for (int chipWidth : chipWidths) {
            int[] chip = placed.place(chipWidth);
            if (chip == null) {
                break;
            }
            chips.add(chip);
        }
        // Leave out chips until the chip of those left out and the button fit after the rest
        for (int shown = chips.size(); shown >= 0; shown--) {
            Rows rest = new Rows(x, y, width, rowHeight, rows);
            for (int i = 0; i < shown; i++) {
                rest.place(chipWidths[i]);
            }
            int hidden = chipWidths.length - shown;
            int[] more = hidden > 0 ? rest.place(moreWidth.applyAsInt(hidden)) : null;
            int[] button = hidden == 0 || more != null ? rest.place(buttonWidth) : null;
            if (button != null) {
                return new ParameterChipLayout(chips.subList(0, shown), more, button);
            }
        }
        // Only if the button does not fit in a single row
        return new ParameterChipLayout(List.of(), chipWidths.length > 0 ? new int[]{x, y, moreWidth.applyAsInt(chipWidths.length)} : null,
                new int[]{x, y, buttonWidth});
    }

    private static class Rows {
        private final int left;
        private final int right;
        private final int rowHeight;
        private final int bottom;
        private int x;
        private int y;

        private Rows(int x, int y, int width, int rowHeight, int rows) {
            this.left = x;
            this.right = x + width;
            this.rowHeight = rowHeight;
            this.bottom = y + rows * rowHeight;
            this.x = x;
            this.y = y;
        }

        @Nullable
        private int[] place(int width) {
            int chipX = x;
            int chipY = y;
            if (chipX > left && chipX + width > right) {
                chipX = left;
                chipY += rowHeight;
            }
            if (chipY >= bottom) {
                return null;
            }
            x = chipX + width + 2;
            y = chipY;
            return new int[]{chipX, chipY, width};
        }
    }

}
