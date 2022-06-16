package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IFluidTypeRenderProperties;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;

/**
 * Renderer for the item inside the {@link org.cyclops.integrateddynamics.block.BlockDryingBasin}.
 *
 * @author rubensworks
 *
 */
public class RenderBlockEntityDryingBasin implements BlockEntityRenderer<BlockEntityDryingBasin> {

    public RenderBlockEntityDryingBasin(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(BlockEntityDryingBasin tile, float partialTicks, PoseStack matrixStack,
                       MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        if(tile != null) {
            if(!tile.getInventory().getItem(0).isEmpty()) {
                matrixStack.pushPose();
                matrixStack.translate(-0.5F, -0.5F, -0.5F);
                renderItem(matrixStack, renderTypeBuffer, tile.getInventory().getItem(0), tile.getRandomRotation());
                matrixStack.popPose();
            }

            FluidStack fluid = tile.getTank().getFluid();
            RenderHelpers.renderFluidContext(fluid, matrixStack, () -> {
                float height = (float) ((fluid.getAmount() * 0.7D) / FluidHelpers.BUCKET_VOLUME + 0.23D + 0.01D);
                int brightness = Math.max(combinedLight, fluid.getFluid().getFluidType().getLightLevel(fluid));
                int l2 = brightness >> 0x10 & 0xFFFF;
                int i3 = brightness & 0xFFFF;

                TextureAtlasSprite icon = RenderHelpers.getFluidIcon(fluid, Direction.UP);
                IFluidTypeRenderProperties renderProperties = RenderProperties.get(fluid.getFluid());
                Triple<Float, Float, Float> color = Helpers.intToRGB(renderProperties.getColorTint(fluid));

                VertexConsumer vb = renderTypeBuffer.getBuffer(RenderType.text(icon.atlas().location()));
                Matrix4f matrix = matrixStack.last().pose();
                vb.vertex(matrix, 0.0625F, height, 0.0625F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU0(), icon.getV1()).uv2(l2, i3).endVertex();
                vb.vertex(matrix, 0.0625F, height, 0.9375F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU0(), icon.getV0()).uv2(l2, i3).endVertex();
                vb.vertex(matrix, 0.9375F, height, 0.9375F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU1(), icon.getV0()).uv2(l2, i3).endVertex();
                vb.vertex(matrix, 0.9375F, height, 0.0625F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU1(), icon.getV1()).uv2(l2, i3).endVertex();
            });
        }
    }

    private void renderItem(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, ItemStack itemStack, float rotation) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0);
        if (model.isGui3d()) {
            matrixStack.translate(1F, 1.2F, 1F);
            matrixStack.scale(1.2F, 1.2F, 1.2F);
        } else {
            matrixStack.translate(1F, 1.2F, 1F);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(25F));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(25F));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotation));
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer, 0);
    }

}
