package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
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
    public void renderValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        Font fontRenderer = context.getFont();
        float maxWidth = 0;

        String[] lines = value.getType().toCompactString(value).getString().split("(?<=[^\\\\])\\\\n");
        for (String line : lines) {
            float width = fontRenderer.width(polishLine(line)) - 1;
            maxWidth = Math.max(maxWidth, width);
        }

        float singleHeight = fontRenderer.lineHeight;
        float totalHeight = singleHeight * lines.length;

        matrixStack.pushPose();

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
            context.getFont().drawInBatch(polishLine(line), 0, offset, color,
                    false, matrixStack.last().pose(), renderTypeBuffer, Font.DisplayMode.NORMAL, 0, combinedLight);
            offset += singleHeight;
        }

        matrixStack.popPose();
    }

    protected String polishLine(String line) {
        return line.replaceAll("\\\\\\\\n", "\\\\n");
    }
}
