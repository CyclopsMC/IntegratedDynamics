package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;

import java.util.Map;

/**
 * Renderer for cable components.
 * @author rubensworks
 */
public class RenderCable implements BlockEntityRenderer<BlockEntityMultipartTicking> {

    private final BlockEntityRendererProvider.Context context;

    public RenderCable(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(BlockEntityMultipartTicking tile, float partialTicks, PoseStack matrixStack,
                       MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        for (Map.Entry<Direction, IPartType<?, ?>> entry : tile.getPartContainer().getParts().entrySet()) {
            for (IPartOverlayRenderer renderer : PartOverlayRenderers.REGISTRY.getRenderers(entry.getValue())) {
                renderer.renderPartOverlay(this.context, tile.getPartContainer(), entry.getKey(), entry.getValue(),
                        partialTicks, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay);
            }
        }
    }

}
