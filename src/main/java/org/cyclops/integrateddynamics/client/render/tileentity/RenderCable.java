package org.cyclops.integrateddynamics.client.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.WorldRendererConsumer;
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
                EnumWorldBlockLayer layer = EnumWorldBlockLayer.TRANSLUCENT;
                ForgeHooksClient.setRenderLayer(layer);
                IBakedModel layerModel = new SimpleBakedModel.Builder(model, Minecraft.getMinecraft().getTextureMapBlocks()
                        .getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage)).makeBakedModel();
                rendererDispatcher.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                startTessellating(x, y, z);
                IVertexConsumer consumer = new WorldRendererConsumer(Tessellator.getInstance().getWorldRenderer());
                renderBreaking(layerModel, consumer);
                finishTessellating();
                ForgeHooksClient.setRenderLayer(EnumWorldBlockLayer.SOLID);
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
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
    }

    private void finishBreaking() {
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    private void startTessellating(double x, double y, double z) {
        Tessellator.getInstance().getWorldRenderer().begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        Tessellator.getInstance().getWorldRenderer().setTranslation(x, y, z);
        Tessellator.getInstance().getWorldRenderer().noColor();
    }

    private void finishTessellating() {
        Tessellator.getInstance().getWorldRenderer().setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();
    }

    private static void renderBreaking(IBakedModel model, IVertexConsumer consumer) {
        for (BakedQuad quad : model.getGeneralQuads()) {
            quad.pipe(consumer);
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            for (BakedQuad quad : model.getFaceQuads(face)) {
                quad.pipe(consumer);
            }
        }
    }

    @Override
    public boolean func_181055_a() {
        return true;
    }
}
