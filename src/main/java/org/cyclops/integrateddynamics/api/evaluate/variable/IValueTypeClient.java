package org.cyclops.integrateddynamics.api.evaluate.variable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * @author rubensworks
 */
public interface IValueTypeClient<V extends IValue> {

    /**
     * Called during ISTER rendering of an variable item.
     * @param value The value to value to render.
     * @param stack The variable stack.
     * @param transformType Transform type.
     * @param matrixStack Matrix stack.
     * @param buffer Render buffer.
     * @param combinedLight Lighting.
     * @param combinedOverlay Overlay.
     */
    public default void renderISTER(V value, ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

    }

}
