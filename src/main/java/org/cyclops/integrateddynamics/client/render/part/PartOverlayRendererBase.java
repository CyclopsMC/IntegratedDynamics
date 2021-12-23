package org.cyclops.integrateddynamics.client.render.part;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

/**
 * Base class for part overlay renderers.
 * @author rubensworks
 */
public abstract class PartOverlayRendererBase implements IPartOverlayRenderer {

    protected int getMaxRenderDistance() {
        return (int) Math.pow(GeneralConfig.partOverlayRenderdistance, 2);
    }

    protected boolean shouldRender(BlockPos pos) {
        if (!NetworkHelpers.shouldWork()) {
            return false;
        }
        Entity renderEntity = Minecraft.getInstance().player;
        return renderEntity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < getMaxRenderDistance();
    }

    /**
     * Sets the OpenGL matrix orientation for the given direction.
     * @param matrixStack The matrix stack.
     * @param direction The direction to orient the OpenGL matrix to.
     */
    protected void setMatrixOrientation(MatrixStack matrixStack, Direction direction) {
        short rotationY = 0;
        short rotationX = 0;
        if (direction == Direction.SOUTH) {
            rotationY = 0;
        } else if (direction == Direction.NORTH) {
            rotationY = 180;
        } else if (direction == Direction.EAST) {
            rotationY = 90;
        } else if (direction == Direction.WEST) {
            rotationY = -90;
        } else if (direction == Direction.UP) {
            rotationX = -90;
        } else if (direction == Direction.DOWN) {
            rotationX = 90;
        }
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotationY));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(rotationX));
    }

}
