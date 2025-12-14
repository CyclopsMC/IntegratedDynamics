package org.cyclops.integrateddynamics.api.client.render.part;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;

/**
 * A renderer used to render additional elements for a part.
 * @author rubensworks
 */
public interface IPartOverlayRenderer {

    /**
     * Render the overlay.
     *
     * @param context             The render context
     * @param partContainer       The part container for this part
     * @param direction           The direction this part is facing
     * @param partType            The part type that is being overlayed
     * @param partialTicks        The partial tick
     * @param matrixStack         The matrix render stack.
     * @param submitNodeCollector The render type buffer.
     * @param combinedLight       The combined light value.
     * @param combinedOverlay     The combined overlay value.
     */
    public void submitPartOverlay(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                                  Direction direction, IPartType partType, float partialTicks, PoseStack matrixStack,
                                  SubmitNodeCollector submitNodeCollector, int combinedLight, int combinedOverlay);

}
