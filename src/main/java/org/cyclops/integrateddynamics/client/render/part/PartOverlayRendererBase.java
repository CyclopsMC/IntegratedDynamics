package org.cyclops.integrateddynamics.client.render.part;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;

/**
 * Base class for part overlay renderers.
 * @author rubensworks
 */
public abstract class PartOverlayRendererBase implements IPartOverlayRenderer {

    /**
     * Sets the OpenGL matrix orientation for the given direction.
     * @param direction The direction to orient the OpenGL matrix to.
     */
    protected void setMatrixOrientation(EnumFacing direction) {
        short rotationY = 0;
        short rotationX = 0;
        if (direction == EnumFacing.SOUTH) {
            rotationY = 0;
        } else if (direction == EnumFacing.NORTH) {
            rotationY = 180;
        } else if (direction == EnumFacing.EAST) {
            rotationY = 90;
        } else if (direction == EnumFacing.WEST) {
            rotationY = -90;
        } else if (direction == EnumFacing.UP) {
            rotationX = -90;
        } else if (direction == EnumFacing.DOWN) {
            rotationX = 90;
        }
        GlStateManager.rotate((float) rotationY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) rotationX, 1.0F, 0.0F, 0.0F);
    }

}
