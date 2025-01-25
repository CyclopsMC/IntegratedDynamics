package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;
import org.joml.Matrix4f;

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
                renderItem(matrixStack, renderTypeBuffer, tile.getInventory().getItem(0), tile.getRandomRotation(), tile.getLevel());
                matrixStack.popPose();
            }

            FluidStack fluid = tile.getTank().getFluid();
            IModHelpersNeoForge.get().getRenderHelpers().renderFluidContext(fluid, matrixStack, () -> {
                float height = (float) ((fluid.getAmount() * 0.7D) / IModHelpersNeoForge.get().getFluidHelpers().getBucketVolume() + 0.23D + 0.01D);
                int brightness = Math.max(combinedLight, fluid.getFluid().getFluidType().getLightLevel(fluid));
                int l2 = brightness >> 0x10 & 0xFFFF;
                int i3 = brightness & 0xFFFF;

                TextureAtlasSprite icon = IModHelpersNeoForge.get().getRenderHelpers().getFluidIcon(fluid, Direction.UP);
                IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid.getFluid());
                Triple<Float, Float, Float> color = IModHelpers.get().getBaseHelpers().intToRGB(renderProperties.getTintColor(fluid));

                VertexConsumer vb = renderTypeBuffer.getBuffer(RenderType.text(icon.atlasLocation()));
                Matrix4f matrix = matrixStack.last().pose();
                vb.addVertex(matrix, 0.0625F, height, 0.0625F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), icon.getV1()).setUv2(l2, i3);
                vb.addVertex(matrix, 0.0625F, height, 0.9375F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), icon.getV0()).setUv2(l2, i3);
                vb.addVertex(matrix, 0.9375F, height, 0.9375F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), icon.getV0()).setUv2(l2, i3);
                vb.addVertex(matrix, 0.9375F, height, 0.0625F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), icon.getV1()).setUv2(l2, i3);
            });
        }
    }

    private void renderItem(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, ItemStack itemStack, float rotation, Level level) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, itemStack, ItemDisplayContext.FIXED, false, level, null, 0);

        if (renderState.isGui3d()) {
            matrixStack.translate(1F, 1.2F, 1F);
            matrixStack.scale(1.2F, 1.2F, 1.2F);
        } else {
            matrixStack.translate(1F, 1.2F, 1F);
            matrixStack.mulPose(Axis.XP.rotationDegrees(25F));
            matrixStack.mulPose(Axis.YP.rotationDegrees(25F));
            matrixStack.mulPose(Axis.YP.rotationDegrees(rotation));
        }

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer, level, 0);
    }

}
