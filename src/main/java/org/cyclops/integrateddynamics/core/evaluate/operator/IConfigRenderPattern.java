package org.cyclops.integrateddynamics.core.evaluate.operator;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Render pattern for operators inside the logic programmer.
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

    @Data
    public static class Base implements IConfigRenderPattern {

        private final int width, height;
        private final Pair<Integer, Integer>[] slotPositions;
        private final Pair<Integer, Integer> symbolPosition;

    }

    public static final IConfigRenderPattern INFIX = new IConfigRenderPattern.Base(100, 22, new Pair[]{Pair.of(2, 2), Pair.of(80, 2)}, Pair.of(45, 2));
    public static final IConfigRenderPattern PREFIX_1 = new IConfigRenderPattern.Base(40, 22, new Pair[]{Pair.of(20, 2)}, Pair.of(6, 2));
    public static final IConfigRenderPattern PREFIX_2 = new IConfigRenderPattern.Base(80, 22, new Pair[]{Pair.of(40, 2), Pair.of(60, 2)}, Pair.of(6, 2));
    public static final IConfigRenderPattern SUFFIX_1 = new IConfigRenderPattern.Base(40, 22, new Pair[]{Pair.of(6, 2)}, Pair.of(26, 2));

}