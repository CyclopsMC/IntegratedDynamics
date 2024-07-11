package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.joml.Matrix4f;

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
            IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            Triple<Float, Float, Float> color = Helpers.intToRGB(renderProperties.getTintColor(fluidStack));

            VertexConsumer vb = renderTypeBuffer.getBuffer(RenderType.text(icon.atlasLocation()));
            Matrix4f matrix = matrixStack.last().pose();

            float min = 0F;
            float max = 12.5F;
            float u1 = icon.getU0();
            float u2 = icon.getU1();
            float v1 = icon.getV0();
            float v2 = icon.getV1();
            vb.addVertex(matrix, max, max, 0).setColor(color.getLeft(), color.getMiddle(), color.getRight(), alpha).setUv(u2, v2).setUv2(l2, i3);
            vb.addVertex(matrix, max, min, 0).setColor(color.getLeft(), color.getMiddle(), color.getRight(), alpha).setUv(u2, v1).setUv2(l2, i3);
            vb.addVertex(matrix, min, min, 0).setColor(color.getLeft(), color.getMiddle(), color.getRight(), alpha).setUv(u1, v1).setUv2(l2, i3);
            vb.addVertex(matrix, min, max, 0).setColor(color.getLeft(), color.getMiddle(), color.getRight(), alpha).setUv(u1, v2).setUv2(l2, i3);

            // Stack size
            matrixStack.translate(7F, 8.5F, 0.1F);
            String string = String.valueOf(fluidStack.getAmount());
            float scale = ((float) 5) / (float) context.getFont().width(string);
            matrixStack.scale(scale, scale, 1F);
            context.getFont().drawInBatch(string,
                    0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)),
                    false, matrixStack.last().pose(), renderTypeBuffer, Font.DisplayMode.NORMAL, 0, combinedLight);
            matrixStack.popPose();
        }
    }

}
