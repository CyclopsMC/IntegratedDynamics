package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntUnaryOperator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests for laying out the parameter chips of the expression panel.
 * @author rubensworks
 */
public class TestParameterChipLayout {

    // As in the panel: rows of 137 wide, 13 apart, at most 4
    private static final int WIDTH = 137;
    private static final int ROW = 13;
    private static final int ROWS = 4;
    private static final IntUnaryOperator MORE = hidden -> 30 + (hidden >= 10 ? 6 : 0);

    private static ParameterChipLayout layout(int buttonWidth, int... widths) {
        return ParameterChipLayout.layout(widths, MORE, buttonWidth, 0, 0, WIDTH, ROW, ROWS);
    }

    private static int[] widths(int count, int width) {
        int[] widths = new int[count];
        java.util.Arrays.fill(widths, width);
        return widths;
    }

    @Test
    public void testNoParameters() {
        ParameterChipLayout layout = layout(60);
        assertThat(layout.chips().size(), is(0));
        assertThat(layout.more(), nullValue());
        assertArrayEquals(new int[]{0, 0, 60}, layout.button());
    }

    @Test
    public void testAllShown() {
        // In one row, followed by the button
        ParameterChipLayout layout = layout(12, 10, 20, 30);
        assertThat(layout.chips().size(), is(3));
        assertArrayEquals(new int[]{0, 0, 10}, layout.chips().get(0));
        assertArrayEquals(new int[]{12, 0, 20}, layout.chips().get(1));
        assertArrayEquals(new int[]{34, 0, 30}, layout.chips().get(2));
        assertThat(layout.more(), nullValue());
        assertArrayEquals(new int[]{66, 0, 12}, layout.button());

        // Wrapping onto the next row
        layout = layout(12, widths(3, 67));
        assertArrayEquals(new int[]{0, ROW, 67}, layout.chips().get(2));
        assertArrayEquals(new int[]{69, ROW, 12}, layout.button());

        // 8 chips of half the width, as 8 of the longest names, fill the 4 rows, with the button next to the last
        layout = layout(12, widths(8, 60));
        assertThat(layout.chips().size(), is(8));
        assertThat(layout.more(), nullValue());
        assertArrayEquals(new int[]{62, 3 * ROW, 60}, layout.chips().get(7));
        assertArrayEquals(new int[]{124, 3 * ROW, 12}, layout.button());
    }

    @Test
    public void testLeftOut() {
        // 9 of half the width: the last row has the chip of those left out and the button
        ParameterChipLayout layout = layout(12, widths(9, 67));
        assertThat(layout.chips().size(), is(7));
        assertArrayEquals(new int[]{0, 3 * ROW, 67}, layout.chips().get(6));
        assertArrayEquals(new int[]{69, 3 * ROW, 30}, layout.more());
        assertArrayEquals(new int[]{101, 3 * ROW, 12}, layout.button());

        // Many short ones
        layout = layout(12, widths(500, 9));
        assertThat(layout.more()[1], is(3 * ROW));
        assertThat(layout.more()[2], is(MORE.applyAsInt(500 - layout.chips().size())));
        assertThat(layout.button()[1], is(3 * ROW));
    }

    @Test
    public void testRandom() {
        // Chips are the first parameters, do not overlap and stay within the rows, with the button last
        Random random = new Random(42);
        for (int run = 0; run < 1000; run++) {
            int count = random.nextInt(60);
            int[] widths = new int[count];
            for (int i = 0; i < count; i++) {
                widths[i] = 5 + random.nextInt(63);
            }
            int buttonWidth = count == 0 ? 60 : 12;
            ParameterChipLayout layout = layout(buttonWidth, widths);
            List<int[]> all = new ArrayList<>(layout.chips());
            for (int i = 0; i < layout.chips().size(); i++) {
                assertThat(layout.chips().get(i)[2], is(widths[i]));
            }
            assertThat(layout.more() == null, is(layout.chips().size() == count));
            if (layout.more() != null) {
                assertThat(layout.more()[2], is(MORE.applyAsInt(count - layout.chips().size())));
                all.add(layout.more());
            }
            all.add(layout.button());
            for (int i = 0; i < all.size(); i++) {
                int[] a = all.get(i);
                assertTrue(a[0] >= 0 && a[0] + a[2] <= WIDTH && a[1] >= 0 && a[1] < ROWS * ROW && a[1] % ROW == 0);
                for (int j = i + 1; j < all.size(); j++) {
                    int[] b = all.get(j);
                    // In order, so later ones are further right on a row, or on a later row
                    assertTrue(b[1] > a[1] || (b[1] == a[1] && b[0] >= a[0] + a[2] + 2));
                }
            }
        }
    }

}
