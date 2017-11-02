package org.cyclops.integrateddynamics.api.logicprogrammer;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Render pattern for elements inside the logic programmer.
 * @author rubensworks
 */
public interface IConfigRenderPattern {

    /**
     * @return Total width.
     */
    public int getWidth();

    /**
     * @return Total height.
     */
    public int getHeight();

    /**
     * @return Top-left positions of the slots to render.
     */
    public Pair<Integer, Integer>[] getSlotPositions();

    /**
     * @return Top-left position of the symbol to render.
     */
    public Pair<Integer, Integer> getSymbolPosition();

    public static class Base implements IConfigRenderPattern {

        private final int width, height;
        private final Pair<Integer, Integer>[] slotPositions;
        private final Pair<Integer, Integer> symbolPosition;

        public Base(int width, int height, Pair<Integer, Integer>[] slotPositions, Pair<Integer, Integer> symbolPosition) {
            this.width = width;
            this.height = height;
            this.slotPositions = slotPositions;
            this.symbolPosition = symbolPosition;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public Pair<Integer, Integer>[] getSlotPositions() {
            return this.slotPositions;
        }

        public Pair<Integer, Integer> getSymbolPosition() {
            return this.symbolPosition;
        }

    }

    public static final IConfigRenderPattern NONE = new IConfigRenderPattern.Base(100, 22, new Pair[0], null);
    public static final IConfigRenderPattern NONE_CANVAS = new IConfigRenderPattern.Base(150, 80, new Pair[0], null);
    public static final IConfigRenderPattern SINGLE_SLOT = new IConfigRenderPattern.Base(22, 22, new Pair[]{Pair.of(2, 2)}, null);
    public static final IConfigRenderPattern RECIPE = new IConfigRenderPattern.Base(136, 89, new Pair[]{
            // Items in
            Pair.of(2, 2), Pair.of(20, 2), Pair.of(38, 2),
            Pair.of(2, 20), Pair.of(20, 20), Pair.of(38, 20),
            Pair.of(2, 38), Pair.of(20, 38), Pair.of(38, 38),
            // Fluid in
            Pair.of(2, 58),
            // Items out
            Pair.of(100, 2),
            Pair.of(100, 20),
            Pair.of(100, 38),
            // Fluid out
            Pair.of(82, 58),
    }, null);
    public static final IConfigRenderPattern INFIX = new IConfigRenderPattern.Base(100, 22, new Pair[]{Pair.of(2, 2), Pair.of(80, 2)}, Pair.of(45, 2));
    public static final IConfigRenderPattern PREFIX_1 = new IConfigRenderPattern.Base(40, 22, new Pair[]{Pair.of(20, 2)}, Pair.of(6, 2));
    public static final IConfigRenderPattern PREFIX_1_LONG = new IConfigRenderPattern.Base(80, 22, new Pair[]{Pair.of(20, 2)}, Pair.of(6, 2));
    public static final IConfigRenderPattern INFIX_2 = new IConfigRenderPattern.Base(120, 22, new Pair[]{Pair.of(2, 2), Pair.of(80, 2), Pair.of(100, 2)}, Pair.of(22, 2));
    public static final IConfigRenderPattern INFIX_2_LATE = new IConfigRenderPattern.Base(120, 22, new Pair[]{Pair.of(2, 2), Pair.of(22, 2), Pair.of(100, 2)}, Pair.of(55, 2));
    public static final IConfigRenderPattern PREFIX_2 = new IConfigRenderPattern.Base(80, 22, new Pair[]{Pair.of(40, 2), Pair.of(60, 2)}, Pair.of(6, 2));
    public static final IConfigRenderPattern PREFIX_2_LONG = new IConfigRenderPattern.Base(100, 22, new Pair[]{Pair.of(60, 2), Pair.of(80, 2)}, Pair.of(6, 2));
    public static final IConfigRenderPattern INFIX_3 = new IConfigRenderPattern.Base(100, 22, new Pair[]{Pair.of(2, 2), Pair.of(80, 2), Pair.of(100, 2), Pair.of(120, 2)}, Pair.of(45, 2));
    public static final IConfigRenderPattern PREFIX_3_LONG = new IConfigRenderPattern.Base(120, 22, new Pair[]{Pair.of(60, 2), Pair.of(80, 2), Pair.of(100, 2)}, Pair.of(6, 2));
    public static final IConfigRenderPattern SUFFIX_1 = new IConfigRenderPattern.Base(40, 22, new Pair[]{Pair.of(6, 2)}, Pair.of(26, 2));
    public static final IConfigRenderPattern SUFFIX_1_LONG = new IConfigRenderPattern.Base(80, 22, new Pair[]{Pair.of(6, 2)}, Pair.of(26, 2));

}