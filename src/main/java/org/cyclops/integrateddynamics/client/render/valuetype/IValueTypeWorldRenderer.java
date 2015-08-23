package org.cyclops.integrateddynamics.client.render.valuetype;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartType;

/**
 * Renderer used to display values for a given value type in the world.
 * @author rubensworks
 */
public interface IValueTypeWorldRenderer {

    /**
     * Render the overlay.
     * @param partContainer The part container for this part.
     * @param x The center block X
     * @param y The center block Y
     * @param z The center block Z
     * @param partialTick The partial tick
     * @param destroyStage The destroy stage
     * @param direction The direction this part is facing.
     * @param partType The part type that is being overlayed
     * @param value The value to render
     * @param rendererDispatcher The render dispatcher
     */
    public void renderValue(IPartContainer partContainer, double x, double y, double z, float partialTick,
                            int destroyStage, EnumFacing direction, IPartType partType, IValue value, TileEntityRendererDispatcher rendererDispatcher);

}
