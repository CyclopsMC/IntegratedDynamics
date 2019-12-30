package org.cyclops.integrateddynamics.client.render.part;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

/**
 * Base class for part overlay renderers.
 * @author rubensworks
 */
public abstract class PartOverlayRendererBase implements IPartOverlayRenderer {

    protected int getMaxRenderDistance() {
        return GeneralConfig.partOverlayRenderdistance;
    }

    protected boolean shouldRender(BlockPos pos) {
        if (!NetworkHelpers.shouldWork()) {
            return false;
        }
        Entity renderEntity = Minecraft.getInstance().player;
        return renderEntity.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < getMaxRenderDistance();
    }

    /**
     * Sets the OpenGL matrix orientation for the given direction.
     * @param direction The direction to orient the OpenGL matrix to.
     */
    protected void setMatrixOrientation(Direction direction) {
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
        GlStateManager.rotatef((float) rotationY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef((float) rotationX, 1.0F, 0.0F, 0.0F);
    }

}
