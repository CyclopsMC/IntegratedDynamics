package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
    public void renderValue(IPartContainer partContainer, double x, double y, double z, float partialTick,
                            int destroyStage, Direction direction, IPartType partType, IValue value,
                            TileEntityRendererDispatcher rendererDispatcher, float alpha) {
        FluidStack fluidStack = ((ValueObjectTypeFluidStack.ValueFluidStack) value).getRawValue();
        if (!fluidStack.isEmpty()) {
            // Fluid
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            BufferBuilder worldRenderer = Tessellator.getInstance().getBuffer();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            TextureAtlasSprite icon = RenderHelpers.getFluidIcon(fluidStack, Direction.NORTH);
            RenderHelpers.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

            float min = 0F;
            float max = 12.5F;
            float u1 = icon.getMinU();
            float u2 = icon.getMaxU();
            float v1 = icon.getMinV();
            float v2 = icon.getMaxV();
            Triple<Float, Float, Float> colorParts = RenderHelpers.getFluidVertexBufferColor(fluidStack);
            float r = colorParts.getLeft();
            float g = colorParts.getMiddle();
            float b = colorParts.getRight();
            worldRenderer.pos((double)max, (double)max, 0).tex((double)u2, (double)v2).color(r, g, b, alpha).endVertex();
            worldRenderer.pos((double)max, (double)min, 0).tex((double)u2, (double)v1).color(r, g, b, alpha).endVertex();
            worldRenderer.pos((double)min, (double)min, 0).tex((double)u1, (double)v1).color(r, g, b, alpha).endVertex();
            worldRenderer.pos((double)min, (double)max, 0).tex((double)u1, (double)v2).color(r, g, b, alpha).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.popMatrix();

            // Stack size
            GlStateManager.pushMatrix();
            GlStateManager.translatef(7F, 8.5F, 0.1F);
            GlStateManager.pushMatrix();
            String string = String.valueOf(fluidStack.getAmount());
            float scale = ((float) 5) / (float) rendererDispatcher.getFontRenderer().getStringWidth(string);
            GlStateManager.scalef(scale, scale, 1F);
            rendererDispatcher.getFontRenderer().drawString(string,
                    0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)));
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }
}
