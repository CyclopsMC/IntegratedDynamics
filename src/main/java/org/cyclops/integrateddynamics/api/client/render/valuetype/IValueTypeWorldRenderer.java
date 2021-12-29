package org.cyclops.integrateddynamics.api.client.render.valuetype;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;

/**
 * Renderer used to display values for a given value type in the world.
 * @author rubensworks
 */
public interface IValueTypeWorldRenderer {

    /**
     * Render the overlay.
     * @param context The render context
     * @param partContainer The part container for this part
     * @param direction The direction this part is facing
     * @param partType The part type that is being overlayed
     * @param value The value to render
     * @param partialTicks The partial tick
     * @param matrixStack The matrix render stack.
     * @param renderTypeBuffer The render type buffer.
     * @param combinedLight The combined light value.
     * @param combinedOverlay The combined overlay value.
     * @param alpha The alpha to render with.
     */
    public void renderValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha);

}
