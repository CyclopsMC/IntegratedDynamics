package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;

import java.util.List;

/**
 * A text-based value type world renderer for lists.
 * @author rubensworks
 */
public class ListValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    private static final float MAX = 12.5F;
    private static final float MARGIN_FACTOR = 1.1F;

    @Override
    public void renderValue(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        FontRenderer fontRenderer = rendererDispatcher.getFontRenderer();
        float maxWidth = 0;

        List<Pair<String, Integer>> lines = Lists.newLinkedList();
        IValueType listType = ((ValueTypeList.ValueList<?, ?>) value).getRawValue().getValueType();
        for(IValue element : ((ValueTypeList.ValueList<?, ?>) value).getRawValue()) {
            if(lines.size() >= ValueTypeList.MAX_RENDER_LINES) {
                lines.add(Pair.of("...", listType.getDisplayColor()));
                break;
            } else {
                IValueType elementType = element.getType();
                String string = " - " + elementType.toCompactString(element).getString();
                float width = fontRenderer.getStringWidth(string) - 1;
                lines.add(Pair.of(string, elementType.getDisplayColor()));
                maxWidth = Math.max(maxWidth, width);
            }
        }

        float singleHeight = fontRenderer.FONT_HEIGHT;
        float totalHeight = singleHeight * lines.size();

        matrixStack.push();

        float scaleX = MAX / (maxWidth * MARGIN_FACTOR);
        float scaleY = MAX / (totalHeight * MARGIN_FACTOR);
        float scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
        float newWidth = maxWidth * scale;
        float newHeight = totalHeight * scale;
        matrixStack.translate((MAX - newWidth) / 2, (MAX - newHeight) / 2, 0F);
        matrixStack.scale(scale, scale, 1F);

        int offset = 0;
        for(Pair<String, Integer> line : lines) {
            int color = Helpers.addAlphaToColor(line.getRight(), alpha);
            rendererDispatcher.getFontRenderer().renderString(line.getLeft(), 0, offset, color,
                    false, matrixStack.getLast().getMatrix(), renderTypeBuffer, false, 0, combinedLight);
            offset += singleHeight;
        }

        matrixStack.pop();
    }
}
