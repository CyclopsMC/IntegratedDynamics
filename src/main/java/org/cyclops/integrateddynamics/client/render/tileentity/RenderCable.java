package org.cyclops.integrateddynamics.client.render.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.core.block.ICollidable;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.Map;

/**
 * Renderer for cable components.
 * Breaking overlay rendering code inspired by MCMultiPart:
 *  https://github.com/amadornes/MCMultiPart/blob/master/src/main/java/mcmultipart/client/multipart/MultipartContainerSpecialRenderer.java
 * @author rubensworks
 */
public class RenderCable extends TileEntitySpecialRenderer<TileMultipartTicking> {

    @Override
    public void renderTileEntityAt(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                      int destroyStage) {
        if (MinecraftForgeClient.getRenderPass() == 0) {
            for (Map.Entry<EnumFacing, IPartType<?, ?>> entry : tile.getParts().entrySet()) {
                for (IPartOverlayRenderer renderer : PartOverlayRenderers.REGISTRY.getRenderers(entry.getValue())) {
                    renderer.renderPartOverlay(tile, x, y, z, partialTick, destroyStage, entry.getKey(), entry.getValue(), rendererDispatcher);
                }
            }
        }

        if (destroyStage >= 0 && MinecraftForgeClient.getRenderPass() == 1) {
            startBreaking(rendererDispatcher);

            ICollidable.RayTraceResult result = BlockCable.getInstance().doRayTrace(tile.getWorld(), tile.getPos(), Minecraft.getMinecraft().thePlayer);
            IBakedModel model = null;
            if(result != null && result.getCollisionType() != null) {
                model = result.getCollisionType().getBreakingBaseModel(tile.getWorld(), tile.getPos(), result.getPositionHit());
            }

            if (model != null) {
                BlockRenderLayer layer = BlockRenderLayer.TRANSLUCENT;
                ForgeHooksClient.setRenderLayer(layer);
                IBlockState blockState = getWorld().getBlockState(tile.getPos());
                IBakedModel layerModel = new SimpleBakedModel.Builder(blockState, model, Minecraft.getMinecraft().getTextureMapBlocks()
                        .getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage), tile.getPos()).makeBakedModel();
                rendererDispatcher.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                startTessellating(x, y, z);
                IVertexConsumer consumer = new VertexBufferConsumer(Tessellator.getInstance().getBuffer());
                renderBreaking(blockState, layerModel, consumer);
                finishTessellating();
                ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);
            }

            finishBreaking();
        }
    }

    private void startBreaking(TileEntityRendererDispatcher rendererDispatcher) {
        GlStateManager.pushMatrix();
        GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
    }

    private void finishBreaking() {
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    private void startTessellating(double x, double y, double z) {
        Tessellator.getInstance().getBuffer().begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        Tessellator.getInstance().getBuffer().setTranslation(x, y, z);
        Tessellator.getInstance().getBuffer().noColor();
    }

    private void finishTessellating() {
        Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();
    }

    private static void renderBreaking(IBlockState blockState, IBakedModel model, IVertexConsumer consumer) {
        for (EnumFacing face : EnumFacing.VALUES) {
            for (BakedQuad quad : model.getQuads(blockState, face, 0L)) {
                quad.pipe(consumer);
            }
        }
    }
}
