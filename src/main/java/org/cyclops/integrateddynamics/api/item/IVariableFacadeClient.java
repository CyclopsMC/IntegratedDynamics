package org.cyclops.integrateddynamics.api.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public interface IVariableFacadeClient {

    /**
     * Handle the quads for the given baked facadeModel.
     *
     * @param variableModelBaked The baked facadeModel.
     */
    @Nullable
    public ItemModel getItemModelOverlay(IVariableModelBaked variableModelBaked);

    /**
     * Called during ISTER rendering of an variable item.
     *
     * @param stack               The variable stack.
     * @param transformType       Transform type.
     * @param matrixStack         Matrix stack.
     * @param submitNodeCollector Node collector.
     * @param combinedLight       Lighting.
     * @param combinedOverlay     Overlay.
     */
    public default void renderISTER(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int combinedLight, int combinedOverlay) {

    }

}
