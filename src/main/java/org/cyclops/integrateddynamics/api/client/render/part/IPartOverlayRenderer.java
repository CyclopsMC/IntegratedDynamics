package org.cyclops.integrateddynamics.api.client.render.part;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;

/**
 * A renderer used to render additional elements for a part.
 * @author rubensworks
 */
public interface IPartOverlayRenderer {

    /**
     * Render the overlay.
     * @param rendererDispatcher The render dispatcher
     * @param partContainer The part container for this part
     * @param direction The direction this part is facing
     * @param partType The part type that is being overlayed
     * @param partialTicks The partial tick
     * @param matrixStack The matrix render stack.
     * @param renderTypeBuffer The render type buffer.
     * @param combinedLight The combined light value.
     * @param combinedOverlay The combined overlay value.
     */
    public void renderPartOverlay(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                                  Direction direction, IPartType partType, float partialTicks, MatrixStack matrixStack,
                                  IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay);

}
