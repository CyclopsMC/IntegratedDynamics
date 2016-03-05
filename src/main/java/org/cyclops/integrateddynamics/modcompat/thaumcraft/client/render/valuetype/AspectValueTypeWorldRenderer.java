package org.cyclops.integrateddynamics.modcompat.thaumcraft.client.render.valuetype;

import com.google.common.base.Optional;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueObjectTypeAspect;
import thaumcraft.api.aspects.Aspect;

/**
 * A text-based value type world renderer for lists.
 * @author rubensworks
 */
public class AspectValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    @Override
    public void renderValue(IPartContainer partContainer, double x, double y, double z, float partialTick,
                            int destroyStage, EnumFacing direction, IPartType partType, IValue value,
                            TileEntityRendererDispatcher rendererDispatcher, float alpha) {
        Optional<Pair<Aspect, Integer>> optional = ((ValueObjectTypeAspect.ValueAspect) value).getRawValue();
        if(optional.isPresent()) {
            ResourceLocation resourceLocation = optional.get().getKey().getImage();
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();

            int sheetX = 0;
            int sheetY = 0;
            int sheetWidth = 32;
            int sheetHeight = 32;
            float min = 0F;
            float max = 12.5F;

            Triple<Float, Float, Float> color = Helpers.intToRGB(optional.get().getKey().getColor());
            float r = color.getLeft();
            float g = color.getMiddle();
            float b = color.getRight();

            // Render aspect
            GlStateManager.pushMatrix();
            WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            rendererDispatcher.renderEngine.bindTexture(resourceLocation);
            float u1 = (float)sheetX / sheetWidth;
            float u2 = (float)(sheetX + sheetWidth) / sheetWidth;
            float v1 = (float)sheetY / sheetHeight;
            float v2 = (float)(sheetY + sheetHeight) / sheetHeight;
            worldRenderer.pos((double)max, (double)max, 0).tex((double)u2, (double)v2).color(r, g, b, alpha).endVertex();
            worldRenderer.pos((double)max, (double)min, 0).tex((double)u2, (double)v1).color(r, g, b, alpha).endVertex();
            worldRenderer.pos((double)min, (double)min, 0).tex((double)u1, (double)v1).color(r, g, b, alpha).endVertex();
            worldRenderer.pos((double)min, (double)max, 0).tex((double)u1, (double)v2).color(r, g, b, alpha).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.popMatrix();

            // Render aspect size
            GlStateManager.translate(7F, 8.5F, 0.1F);
            GlStateManager.scale(0.5F, 0.5F, 1F);
            rendererDispatcher.getFontRenderer().drawString(String.valueOf(optional.get().getValue()), 0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)));

            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }
    }
}
