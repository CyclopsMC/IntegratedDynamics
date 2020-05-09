package org.cyclops.integrateddynamics.client.render.valuetype;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.DisplayPartOverlayRenderer;

/**
 * A simple text-based value type world renderer.
 * @author rubensworks
 */
public class TextValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    private static final float MARGIN_FACTOR = 1.1F;

    @Override
    public void renderValue(IPartContainer partContainer, double x, double y, double z, float partialTick,
                            int destroyStage, EnumFacing direction, IPartType partType, IValue value,
                            TileEntityRendererDispatcher rendererDispatcher, float distanceAlpha) {
        FontRenderer fontRenderer = rendererDispatcher.getFontRenderer();
        float maxWidth = 0;

        String[] lines = value.getType().toCompactString(value).split("(?<=[^\\\\])\\\\n");
        for (String line : lines) {
            float width = fontRenderer.getStringWidth(polishLine(line)) - 1;
            maxWidth = Math.max(maxWidth, width);
        }

        float singleHeight = fontRenderer.FONT_HEIGHT;
        float totalHeight = singleHeight * lines.length;

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();

        float scaleX = DisplayPartOverlayRenderer.MAX / (maxWidth * MARGIN_FACTOR);
        float scaleY = DisplayPartOverlayRenderer.MAX / (totalHeight * MARGIN_FACTOR);
        float scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
        float newWidth = maxWidth * scale;
        float newHeight = totalHeight * scale;
        GlStateManager.translate((DisplayPartOverlayRenderer.MAX - newWidth) / 2, (DisplayPartOverlayRenderer.MAX - newHeight) / 2, 0F);
        GlStateManager.scale(scale, scale, 1F);

        int offset = 0;
        for(String line : lines) {
            int color = Helpers.addAlphaToColor(value.getType().getDisplayColor(), distanceAlpha);
            rendererDispatcher.getFontRenderer().drawString(polishLine(line), 0, offset, color);
            offset += singleHeight;
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    protected String polishLine(String line) {
        return line.replaceAll("\\\\\\\\n", "\\\\n");
    }
}
