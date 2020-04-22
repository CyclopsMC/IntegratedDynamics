package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
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
    public void renderValue(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        FluidStack fluidStack = ((ValueObjectTypeFluidStack.ValueFluidStack) value).getRawValue();
        if (!fluidStack.isEmpty()) {
            int brightness = Math.max(combinedLight, fluidStack.getFluid().getAttributes().getLuminosity(fluidStack));
            int l2 = brightness >> 0x10 & 0xFFFF;
            int i3 = brightness & 0xFFFF;

            // Fluid
            matrixStack.push();
            TextureAtlasSprite icon = RenderHelpers.getFluidIcon(fluidStack, Direction.UP);
            Triple<Float, Float, Float> color = Helpers.intToRGB(fluidStack.getFluid().getAttributes().getColor(rendererDispatcher.world, rendererDispatcher.renderInfo.getBlockPos()));

            IVertexBuilder vb = renderTypeBuffer.getBuffer(RenderType.getText(icon.getAtlasTexture().getTextureLocation()));
            Matrix4f matrix = matrixStack.getLast().getMatrix();

            float min = 0F;
            float max = 12.5F;
            float u1 = icon.getMinU();
            float u2 = icon.getMaxU();
            float v1 = icon.getMinV();
            float v2 = icon.getMaxV();
            vb.pos(matrix, max, max, 0).color(color.getLeft(), color.getMiddle(), color.getRight(), alpha).tex(u2, v2).lightmap(l2, i3).endVertex();
            vb.pos(matrix, max, min, 0).color(color.getLeft(), color.getMiddle(), color.getRight(), alpha).tex(u2, v1).lightmap(l2, i3).endVertex();
            vb.pos(matrix, min, min, 0).color(color.getLeft(), color.getMiddle(), color.getRight(), alpha).tex(u1, v1).lightmap(l2, i3).endVertex();
            vb.pos(matrix, min, max, 0).color(color.getLeft(), color.getMiddle(), color.getRight(), alpha).tex(u1, v2).lightmap(l2, i3).endVertex();

            // Stack size
            matrixStack.translate(7F, 8.5F, 0.1F);
            String string = String.valueOf(fluidStack.getAmount());
            float scale = ((float) 5) / (float) rendererDispatcher.getFontRenderer().getStringWidth(string);
            matrixStack.scale(scale, scale, 1F);
            rendererDispatcher.getFontRenderer().renderString(string,
                    0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)),
                    false, matrixStack.getLast().getMatrix(), renderTypeBuffer, false, 0, combinedLight);
            matrixStack.pop();
        }
    }

}
