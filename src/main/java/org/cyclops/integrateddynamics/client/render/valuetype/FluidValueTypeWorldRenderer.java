package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.IFluidTypeRenderProperties;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;

/**
 * A value type world renderer for fluids.
 * @author rubensworks
 */
public class FluidValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    @Override
    public void renderValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        FluidStack fluidStack = ((ValueObjectTypeFluidStack.ValueFluidStack) value).getRawValue();
        if (!fluidStack.isEmpty()) {
            int brightness = Math.max(combinedLight, fluidStack.getFluid().getFluidType().getLightLevel(fluidStack));
            int l2 = brightness >> 0x10 & 0xFFFF;
            int i3 = brightness & 0xFFFF;

            // Fluid
            matrixStack.pushPose();
            TextureAtlasSprite icon = RenderHelpers.getFluidIcon(fluidStack, Direction.UP);
            IFluidTypeRenderProperties renderProperties = RenderProperties.get(fluidStack.getFluid());
            Triple<Float, Float, Float> color = Helpers.intToRGB(renderProperties.getColorTint(fluidStack));

            VertexConsumer vb = renderTypeBuffer.getBuffer(RenderType.text(icon.atlas().location()));
            Matrix4f matrix = matrixStack.last().pose();

            float min = 0F;
            float max = 12.5F;
            float u1 = icon.getU0();
            float u2 = icon.getU1();
            float v1 = icon.getV0();
            float v2 = icon.getV1();
            vb.vertex(matrix, max, max, 0).color(color.getLeft(), color.getMiddle(), color.getRight(), alpha).uv(u2, v2).uv2(l2, i3).endVertex();
            vb.vertex(matrix, max, min, 0).color(color.getLeft(), color.getMiddle(), color.getRight(), alpha).uv(u2, v1).uv2(l2, i3).endVertex();
            vb.vertex(matrix, min, min, 0).color(color.getLeft(), color.getMiddle(), color.getRight(), alpha).uv(u1, v1).uv2(l2, i3).endVertex();
            vb.vertex(matrix, min, max, 0).color(color.getLeft(), color.getMiddle(), color.getRight(), alpha).uv(u1, v2).uv2(l2, i3).endVertex();

            // Stack size
            matrixStack.translate(7F, 8.5F, 0.1F);
            String string = String.valueOf(fluidStack.getAmount());
            float scale = ((float) 5) / (float) context.getFont().width(string);
            matrixStack.scale(scale, scale, 1F);
            context.getFont().drawInBatch(string,
                    0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)),
                    false, matrixStack.last().pose(), renderTypeBuffer, false, 0, combinedLight);
            matrixStack.popPose();
        }
    }

}
