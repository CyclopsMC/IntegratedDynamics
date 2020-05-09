package org.cyclops.integrateddynamics.client.render.part;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRenderers;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;

/**
 * Overlay renderer for the display part to display values on the part.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public class DisplayPartOverlayRenderer extends PartOverlayRendererBase {

    public static final float MAX = 12.5F;
    protected static final float pixel = 0.0625F;  // 0.0625 == 1/16

    @Override
    protected void setMatrixOrientation(EnumFacing direction) {
        super.setMatrixOrientation(direction);
        float translateX = -1F - direction.getXOffset() + 4 * pixel;
        float translateY = 1F - direction.getYOffset() - 4 * pixel;
        float translateZ = direction.getZOffset() - pixel + 0.0025F;
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
        BlockPos pos = partContainer.getPosition().getBlockPos();
        if(!shouldRender(pos)) return;

        // Calculate the alpha to be used when the player is almost out of rendering bounds.
        Entity renderEntity = FMLClientHandler.instance().getClient().player;
        float distanceFactor = (float) ((getMaxRenderDistance() - renderEntity.getDistance(pos.getX(), pos.getY(), pos.getZ())) / 5);
        float distanceAlpha = Math.min(1.0F, distanceFactor);
        if(distanceAlpha < 0.05F) distanceAlpha = 0.05F; // Can't be 0 because the MC font renderer doesn't handle 0 alpha's properly.

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.disableLighting();

        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float scale = 0.04F;
        GlStateManager.translate((float) x, (float) y, (float) z);
        setMatrixOrientation(direction);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.scale(1, -1, 1);
        GlStateManager.disableRescaleNormal();

        IPartState partStateUnsafe = partContainer.getPartState(direction);
        if(!(partStateUnsafe instanceof PartTypePanelDisplay.State)) {
            drawError(rendererDispatcher, distanceAlpha);
        } else {
            PartTypePanelDisplay.State partState = (PartTypePanelDisplay.State) partStateUnsafe;
            if (partState.getFacingRotation() == null) {
                drawError(rendererDispatcher, distanceAlpha);
                return;
            }
            int rotation = partState.getFacingRotation().ordinal() - 2;
            GlStateManager.translate(6, 6, 0);
            GlStateManager.rotate(rotation * 90, 0, 0, 1);
            GlStateManager.translate(-6, -6, 0);

            IValue value = partState.getDisplayValue();
            if (value != null && partState.isEnabled()) {
                IValueType<?> valueType = value.getType();
                IValueTypeWorldRenderer renderer = ValueTypeWorldRenderers.REGISTRY.getRenderer(valueType);
                if (renderer == null) {
                    renderer = ValueTypeWorldRenderers.DEFAULT;
                }
                renderer.renderValue(partContainer, x, y, z, partialTick, destroyStage, direction, partType, value, rendererDispatcher, distanceAlpha);
            } else if (!partState.getInventory().isEmpty()) {
                drawError(rendererDispatcher, distanceAlpha);
            }
        }

        GlStateManager.enableLighting();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void drawError(TileEntityRendererDispatcher rendererDispatcher, float distanceAlpha) {
        Images.ERROR.drawWorldWithAlpha(rendererDispatcher.renderEngine, 12.5F, 12.5F, distanceAlpha);
    }
}
