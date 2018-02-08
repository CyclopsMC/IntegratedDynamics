package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;

/**
 * A text-based value type world renderer for operators.
 * @author rubensworks
 */
public class OperatorValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    private static final int MAX_LINES = 30;
    private static final float MAX = 12.5F;
    private static final float MARGIN_FACTOR = 1.1F;

    @Override
    public void renderValue(IPartContainer partContainer, double x, double y, double z, float partialTick,
                            int destroyStage, EnumFacing direction, IPartType partType, IValue value,
                            TileEntityRendererDispatcher rendererDispatcher, float distanceAlpha) {
        FontRenderer fontRenderer = rendererDispatcher.getFontRenderer();
        float maxWidth = 0;

        ValueTypeOperator.ValueOperator valueOperator = ((ValueTypeOperator.ValueOperator) value);
        IOperator operator = valueOperator.getRawValue();
        List<String> lines = Lists.newLinkedList();
        lines.add(ValueTypes.OPERATOR.getName(valueOperator) + " ::");
        lines.addAll(ValueTypeOperator.getSignatureLines(operator, true));
        for (String line : lines) {
            float width = fontRenderer.getStringWidth(line) - 1;
            maxWidth = Math.max(maxWidth, width);
        }

        float singleHeight = fontRenderer.FONT_HEIGHT;
        float totalHeight = singleHeight * lines.size();

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();

        float scaleX = MAX / (maxWidth * MARGIN_FACTOR);
        float scaleY = MAX / (totalHeight * MARGIN_FACTOR);
        float scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
        float newWidth = maxWidth * scale;
        float newHeight = totalHeight * scale;
        GlStateManager.translate((MAX - newWidth) / 2, (MAX - newHeight) / 2, 0F);
        GlStateManager.scale(scale, scale, 1F);

        int offset = 0;
        for(String line : lines) {
            int color = Helpers.addAlphaToColor(ValueTypes.OPERATOR.getDisplayColor(), distanceAlpha);
            rendererDispatcher.getFontRenderer().drawString(line, 0, offset, color);
            offset += singleHeight;
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }
}
