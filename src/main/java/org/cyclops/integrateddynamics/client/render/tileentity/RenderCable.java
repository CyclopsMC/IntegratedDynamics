package org.cyclops.integrateddynamics.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.Map;
import java.util.Random;

/**
 * Renderer for cable components.
 * Breaking overlay rendering code inspired by MCMultiPart:
 *  https://github.com/amadornes/MCMultiPart/blob/master/src/main/java/mcmultipart/client/multipart/MultipartContainerSpecialRenderer.java
 * @author rubensworks
 */
public class RenderCable extends TileEntityRenderer<TileMultipartTicking> {

    private static final Random rand = new Random();

    @Override
    public void render(TileMultipartTicking tile, double x, double y, double z, float partialTick, int destroyStage) {
        // Pass 0
        for (Map.Entry<Direction, IPartType<?, ?>> entry : tile.getPartContainer().getParts().entrySet()) {
            for (IPartOverlayRenderer renderer : PartOverlayRenderers.REGISTRY.getRenderers(entry.getValue())) {
                renderer.renderPartOverlay(tile.getPartContainer(), x, y, z, partialTick, destroyStage, entry.getKey(), entry.getValue(), rendererDispatcher);
            }
        }

        // Pass 1
        startBreaking(rendererDispatcher);

        BlockRayTraceResultComponent result = RegistryEntries.BLOCK_CABLE.getShape(tile.getBlockState(), tile.getWorld(), tile.getPos(), ISelectionContext.forEntity(Minecraft.getInstance().player))
                .rayTrace(tile.getPos(), Minecraft.getInstance().player);
        IBakedModel model = null;
        if(result != null) {
            model = result.getComponent().getBreakingBaseModel(tile.getWorld(), tile.getPos());
        }

        if (model != null) {
            BlockRenderLayer layer = BlockRenderLayer.TRANSLUCENT;
            ForgeHooksClient.setRenderLayer(layer);
            BlockState blockState = getWorld().getBlockState(tile.getPos());
            IBakedModel layerModel = new SimpleBakedModel.Builder(blockState, model, Minecraft.getInstance().getTextureMap()
                    .getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage), rand, tile.getPos().toLong()).build();
            RenderHelpers.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            startTessellating(x, y, z);
            IVertexConsumer consumer = new VertexBufferConsumer(Tessellator.getInstance().getBuffer());
            renderBreaking(blockState, layerModel, consumer);
            finishTessellating();
            ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);
        }

        finishBreaking();
    }

    private void startBreaking(TileEntityRendererDispatcher rendererDispatcher) {
        GlStateManager.pushMatrix();
        GlStateManager.blendFuncSeparate(774, 768, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.polygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlphaTest();
        Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
    }

    private void finishBreaking() {
        Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.disableDepthTest();
        GlStateManager.polygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlphaTest();
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

    private static void renderBreaking(BlockState blockState, IBakedModel model, IVertexConsumer consumer) {
        for (Direction face : Direction.values()) {
            for (BakedQuad quad : model.getQuads(blockState, face, rand, EmptyModelData.INSTANCE)) {
                quad.pipe(consumer);
            }
        }
    }
}
