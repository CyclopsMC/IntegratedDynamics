package org.cyclops.integrateddynamics.client.render.valuetype;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;

/**
 * A simple text-based value type world renderer.
 * @author rubensworks
 */
public class TextValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    private static final float MAX = 12.5F;
    private static final float MARGIN_FACTOR = 1.1F;

    @Override
    public void renderValue(IPartContainer partContainer, double x, double y, double z, float partialTick,
                            int destroyStage, EnumFacing direction, IPartType partType, IValue value,
                            TileEntityRendererDispatcher rendererDispatcher) {
        String string = value.getType().toCompactString(value);
        FontRenderer fontRenderer = rendererDispatcher.getFontRenderer();
        float height = fontRenderer.FONT_HEIGHT;
        float width = fontRenderer.getStringWidth(string) - 1;
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();

        float scaleX = MAX / (width * MARGIN_FACTOR);
        float scaleY = MAX / (height * MARGIN_FACTOR);
        float scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
        float newWidth = width * scale;
        float newHeight = height * scale;
        GlStateManager.translate((MAX - newWidth) / 2, (MAX - newHeight) / 2, 0F);
        GlStateManager.scale(scale, scale, 1F);

        rendererDispatcher.getFontRenderer().drawString(string, 0, 0, value.getType().getDisplayColor());
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }
}
