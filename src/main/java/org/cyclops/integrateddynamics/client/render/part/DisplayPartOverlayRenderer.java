package org.cyclops.integrateddynamics.client.render.part;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
@OnlyIn(Dist.CLIENT)
public class DisplayPartOverlayRenderer extends PartOverlayRendererBase {

    public static final float MAX = 12.5F;
    protected static final float pixel = 0.0625F;  // 0.0625 == 1/16

    @Override
    protected void setMatrixOrientation(MatrixStack matrixStack, Direction direction) {
        super.setMatrixOrientation(matrixStack, direction);
        float translateX = -1F - direction.getStepX() + 4 * pixel;
        float translateY = 1F - direction.getStepY() - 4 * pixel;
        float translateZ = direction.getStepZ() - pixel + 0.0025F;
        if (direction == Direction.NORTH) {
            translateZ += 1F;
        } else if (direction == Direction.EAST) {
            translateX += 1F;
            translateZ += 1F;
        } else if (direction == Direction.SOUTH) {
            translateX += 1F;
        } else if (direction == Direction.UP) {
            translateX += 1F;
            translateZ += 1F;
        } else if (direction == Direction.DOWN) {
            translateX += 1F;
            translateY -= 1F;
        }
        matrixStack.translate(translateX, translateY, translateZ);
    }

    @Override
    public void renderPartOverlay(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                                  Direction direction, IPartType partType, float partialTicks, MatrixStack matrixStack,
                                  IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay) {
        BlockPos pos = partContainer.getPosition().getBlockPos();
        if(!shouldRender(pos)) return;

        // Calculate the alpha to be used when the player is almost out of rendering bounds.
        Entity renderEntity = Minecraft.getInstance().player;
        float distanceFactor = (float) ((getMaxRenderDistance() - renderEntity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())) / 5);
        float distanceAlpha = Math.min(1.0F, distanceFactor);
        if(distanceAlpha < 0.05F) distanceAlpha = 0.05F; // Can't be 0 because the MC font renderer doesn't handle 0 alpha's properly.

        matrixStack.pushPose();

        float scale = 0.04F;
        setMatrixOrientation(matrixStack, direction);
        matrixStack.scale(scale, scale, scale);
        matrixStack.scale(1, -1, 1);

        IPartState partStateUnsafe = partContainer.getPartState(direction);
        if(!(partStateUnsafe instanceof PartTypePanelDisplay.State)) {
            drawError(rendererDispatcher, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, distanceAlpha);
        } else {
            PartTypePanelDisplay.State partState = (PartTypePanelDisplay.State) partStateUnsafe;
            if (partState.getFacingRotation() == null) {
                drawError(rendererDispatcher, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, distanceAlpha);
                return;
            }
            int rotation = partState.getFacingRotation().ordinal() - 2;
            matrixStack.translate(6, 6, 0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(rotation * 90));
            matrixStack.translate(-6, -6, 0);

            IValue value = partState.getDisplayValue();
            if (value != null && partState.isEnabled()) {
                IValueType<?> valueType = value.getType();
                IValueTypeWorldRenderer renderer = ValueTypeWorldRenderers.REGISTRY.getRenderer(valueType);
                if (renderer == null) {
                    renderer = ValueTypeWorldRenderers.DEFAULT;
                }
                renderer.renderValue(rendererDispatcher, partContainer, direction, partType, value, partialTicks, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, distanceAlpha);
            } else if (!partState.getInventory().isEmpty()) {
                drawError(rendererDispatcher, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, distanceAlpha);
            }
        }

        matrixStack.popPose();
    }

    protected void drawError(TileEntityRendererDispatcher rendererDispatcher, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
                             int combinedLight, int combinedOverlay, float distanceAlpha) {
        Images.ERROR.drawWorldWithAlpha(rendererDispatcher.textureManager, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, 12.5F, 12.5F, distanceAlpha);
    }
}
