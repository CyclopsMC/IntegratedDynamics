package org.cyclops.integrateddynamics.client.render.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.client.render.tileentity.RenderTileEntityBakedModel;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.Map;

/**
 * Renderer for cable components.
 * @author rubensworks
 */
public class RenderCable extends RenderTileEntityBakedModel<TileMultipartTicking> {

    private IBlockState tempBlockState;

    protected void renderTileEntityAt(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                      int destroyStage) {
        if (destroyStage >= 0) {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else if(getTexture() != null) {
            this.bindTexture(getTexture());
        }

        for(Map.Entry<EnumFacing, IPartType<?, ?>> entry : tile.getParts().entrySet()) {
            tempBlockState = entry.getValue().getBlockState(tile, x, y, z, partialTick, destroyStage, entry.getKey());
            super.renderTileEntityAt(tile, x, y, z, partialTick, destroyStage);
            for(IPartOverlayRenderer renderer: PartOverlayRenderers.REGISTRY.getRenderers(entry.getValue())) {
                renderer.renderPartOverlay(tile, x, y, z, partialTick, destroyStage, entry.getKey(), entry.getValue(), rendererDispatcher);
            }
        }
    }

    @Override
    protected IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                        int destroyStage) {
        return tempBlockState;
    }

}
