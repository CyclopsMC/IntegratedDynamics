package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
    public void renderValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        Font fontRenderer = context.getFont();
        float maxWidth = 0;

        ValueTypeOperator.ValueOperator valueOperator = ((ValueTypeOperator.ValueOperator) value);
        IOperator operator = valueOperator.getRawValue();
        List<Component> lines = Lists.newLinkedList();
        lines.add(Component.literal(ValueTypes.OPERATOR.getName(valueOperator) + " ::"));
        lines.addAll(ValueTypeOperator.getSignatureLines(operator, true));
        for (Component line : lines) {
            float width = fontRenderer.width(line.getString()) - 1;
            maxWidth = Math.max(maxWidth, width);
        }

        float singleHeight = fontRenderer.lineHeight;
        float totalHeight = singleHeight * lines.size();

        matrixStack.pushPose();

        float scaleX = MAX / (maxWidth * MARGIN_FACTOR);
        float scaleY = MAX / (totalHeight * MARGIN_FACTOR);
        float scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
        float newWidth = maxWidth * scale;
        float newHeight = totalHeight * scale;
        matrixStack.translate((MAX - newWidth) / 2, (MAX - newHeight) / 2, 0F);
        matrixStack.scale(scale, scale, 1F);

        int offset = 0;
        for(Component line : lines) {
            int color = Helpers.addAlphaToColor(ValueTypes.OPERATOR.getDisplayColor(), alpha);
            context.getFont().drawInBatch(line.getString(), 0, offset, color,
                    false, matrixStack.last().pose(), renderTypeBuffer, false, 0, combinedLight);
            offset += singleHeight;
        }

        matrixStack.popPose();
    }
}
