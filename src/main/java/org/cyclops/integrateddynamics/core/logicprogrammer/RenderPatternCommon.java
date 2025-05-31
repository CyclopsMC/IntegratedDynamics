package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;

/**
 * @author rubensworks
 */
public class RenderPatternCommon {
    public static int calculateX(int baseX, int maxWidth, IConfigRenderPattern configRenderPattern) {
        return baseX + (maxWidth  - configRenderPattern.getWidth()) / 2;
    }

    public static int calculateY(int baseY, int maxHeight, IConfigRenderPattern configRenderPattern) {
        return baseY + (maxHeight - configRenderPattern.getHeight()) / 2;
    }
}
