package org.cyclops.integrateddynamics.api.client.render.part;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
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
     * @param partContainer The part container for this part.
     * @param x The center block X
     * @param y The center block Y
     * @param z The center block Z
     * @param partialTick The partial tick
     * @param destroyStage The destroy stage
     * @param direction The direction this part is facing
     * @param partType The part type that is being overlayed
     * @param rendererDispatcher The render dispatcher
     */
    public void renderPartOverlay(IPartContainer partContainer, double x, double y, double z, float partialTick,
                                  int destroyStage, Direction direction, IPartType partType, TileEntityRendererDispatcher rendererDispatcher);

}
