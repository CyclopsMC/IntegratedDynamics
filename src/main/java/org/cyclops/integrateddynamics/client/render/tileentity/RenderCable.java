package org.cyclops.integrateddynamics.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.Map;
import java.util.Random;

/**
 * Renderer for cable components.
 * @author rubensworks
 */
public class RenderCable extends TileEntityRenderer<TileMultipartTicking> {

    private static final Random rand = new Random();

    public RenderCable(TileEntityRendererDispatcher tileEntityRendererDispatcher) {
        super(tileEntityRendererDispatcher);
    }

    @Override
    public void render(TileMultipartTicking tile, float partialTicks, MatrixStack matrixStack,
                       IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay) {
        for (Map.Entry<Direction, IPartType<?, ?>> entry : tile.getPartContainer().getParts().entrySet()) {
            for (IPartOverlayRenderer renderer : PartOverlayRenderers.REGISTRY.getRenderers(entry.getValue())) {
                renderer.renderPartOverlay(renderDispatcher, tile.getPartContainer(), entry.getKey(), entry.getValue(),
                        partialTicks, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay);
            }
        }
    }

}
