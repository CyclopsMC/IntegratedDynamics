package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
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
    public void renderValue(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        FontRenderer fontRenderer = rendererDispatcher.getFontRenderer();
        float maxWidth = 0;

        String[] lines = value.getType().toCompactString(value).getString().split("(?<=[^\\\\])\\\\n");
        for (String line : lines) {
            float width = fontRenderer.getStringWidth(polishLine(line)) - 1;
            maxWidth = Math.max(maxWidth, width);
        }

        float singleHeight = fontRenderer.FONT_HEIGHT;
        float totalHeight = singleHeight * lines.length;

        matrixStack.push();

        float scaleX = DisplayPartOverlayRenderer.MAX / (maxWidth * MARGIN_FACTOR);
        float scaleY = DisplayPartOverlayRenderer.MAX / (totalHeight * MARGIN_FACTOR);
        float scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
        float newWidth = maxWidth * scale;
        float newHeight = totalHeight * scale;
        matrixStack.translate((DisplayPartOverlayRenderer.MAX - newWidth) / 2, (DisplayPartOverlayRenderer.MAX - newHeight) / 2, 0F);
        matrixStack.scale(scale, scale, 1F);

        int offset = 0;
        for(String line : lines) {
            int color = Helpers.addAlphaToColor(value.getType().getDisplayColor(), alpha);
            rendererDispatcher.getFontRenderer().renderString(polishLine(line), 0, offset, color,
                    false, matrixStack.getLast().getMatrix(), renderTypeBuffer, false, 0, combinedLight);
            offset += singleHeight;
        }

        matrixStack.pop();
    }

    protected String polishLine(String line) {
        return line.replaceAll("\\\\\\\\n", "\\\\n");
    }
}
