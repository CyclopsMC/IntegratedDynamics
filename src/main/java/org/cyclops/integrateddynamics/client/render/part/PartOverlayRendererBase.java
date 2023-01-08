package org.cyclops.integrateddynamics.client.render.part;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
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
    protected void setMatrixOrientation(PoseStack matrixStack, Direction direction) {
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
        matrixStack.mulPose(Axis.YP.rotationDegrees(rotationY));
        matrixStack.mulPose(Axis.XP.rotationDegrees(rotationX));
    }

}
