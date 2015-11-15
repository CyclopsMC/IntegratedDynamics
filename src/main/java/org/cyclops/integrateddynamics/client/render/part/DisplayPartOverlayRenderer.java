package org.cyclops.integrateddynamics.client.render.part;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.integrateddynamics.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRenderers;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.part.PartTypeDisplay;

/**
 * Overlay renderer for the display part to display values on the part.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public class DisplayPartOverlayRenderer extends PartOverlayRendererBase {

    protected static final float pixel = 0.0625F;  // 0.0625 == 1/16

    @Override
    protected void setMatrixOrientation(EnumFacing direction) {
        super.setMatrixOrientation(direction);
        float translateX = -1F - direction.getFrontOffsetX() + 4 * pixel;
        float translateY = 1F - direction.getFrontOffsetY() - 4 * pixel;
        float translateZ = direction.getFrontOffsetZ() - pixel + 0.0025F;
        if (direction == EnumFacing.NORTH) {
            translateZ += 1F;
        } else if (direction == EnumFacing.EAST) {
            translateX += 1F;
            translateZ += 1F;
        } else if (direction == EnumFacing.SOUTH) {
            translateX += 1F;
        } else if (direction == EnumFacing.UP) {
            translateX += 1F;
            translateZ += 1F;
        } else if (direction == EnumFacing.DOWN) {
            translateX += 1F;
            translateY -= 1F;
        }
        GlStateManager.translate(translateX, translateY, translateZ);
    }

    @Override
    public void renderPartOverlay(IPartContainer partContainer, double x, double y, double z, float partialTick,
                                  int destroyStage, EnumFacing direction, IPartType partType,
                                  TileEntityRendererDispatcher rendererDispatcher) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float scale = 0.04F;
        GlStateManager.translate((float) x, (float) y, (float) z);
        setMatrixOrientation(direction);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.scale(1, -1, 1);
        GlStateManager.disableRescaleNormal();

        PartTypeDisplay.State partState = (PartTypeDisplay.State) partContainer.getPartState(direction);

        int rotation = partState.getFacingRotation().ordinal() - 2;
        GlStateManager.translate(6, 6, 0);
        GlStateManager.rotate(rotation * 90, 0, 0, 1);
        //GlStateManager.rotate((float) (Math.random() * 360), 0, 0, 1);
        GlStateManager.translate(-6, -6, 0);

        IValue value = partState.getDisplayValue();
        if(value != null) {
            IValueType<?> valueType = value.getType();
            IValueTypeWorldRenderer renderer = ValueTypeWorldRenderers.REGISTRY.getRenderer(valueType);
            if(renderer == null) {
                renderer = ValueTypeWorldRenderers.DEFAULT;
            }
            renderer.renderValue(partContainer, x, y, z, partialTick, destroyStage, direction, partType, value, rendererDispatcher);
        } else if(!partState.getInventory().isEmpty()) {
            Images.ERROR.drawWorld(rendererDispatcher.renderEngine, 12.5F, 12.5F);
        }

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
