package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.DirectionHelpers;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.blockentity.BlockEntitySqueezer;
import org.joml.Matrix4f;

/**
 * Renderer for the item inside the {@link org.cyclops.integrateddynamics.block.BlockDryingBasin}.
 *
 * @author rubensworks
 *
 */
public class RenderBlockEntitySqueezer implements BlockEntityRenderer<BlockEntitySqueezer> {

    private static final float OFFSET = 0.01F;
    private static final float MINY = 0.0625F;
    private static final float MAXY = 0.125F - OFFSET;
    private static final float MIN = 0F + OFFSET;
    private static final float MAX = 1F - OFFSET;
    private static float[][][] coordinates = {
            { // DOWN
                    {MIN, MINY, MIN},
                    {MIN, MINY, MAX},
                    {MAX, MINY, MAX},
                    {MAX, MINY, MIN}
            },
            { // UP
                    {MIN, MAXY, MIN},
                    {MIN, MAXY, MAX},
                    {MAX, MAXY, MAX},
                    {MAX, MAXY, MIN}
            },
            { // NORTH
                    {MIN, MINY, MIN},
                    {MIN, MAXY, MIN},
                    {MAX, MAXY, MIN},
                    {MAX, MINY, MIN}
            },
            { // SOUTH
                    {MIN, MINY, MAX},
                    {MIN, MAXY, MAX},
                    {MAX, MAXY, MAX},
                    {MAX, MINY, MAX}
            },
            { // WEST
                    {MIN, MINY, MIN},
                    {MIN, MAXY, MIN},
                    {MIN, MAXY, MAX},
                    {MIN, MINY, MAX}
            },
            { // EAST
                    {MAX, MINY, MIN},
                    {MAX, MAXY, MIN},
                    {MAX, MAXY, MAX},
                    {MAX, MINY, MAX}
            }
    };

    public RenderBlockEntitySqueezer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(BlockEntitySqueezer tile, float partialTicks, PoseStack matrixStack,
                       MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        if(tile != null) {
            if(!tile.getInventory().getItem(0).isEmpty()) {
                matrixStack.pushPose();
                matrixStack.translate(-0.5F, -0.5F, -0.5F);
                renderItem(matrixStack, renderTypeBuffer, tile.getInventory().getItem(0), tile);
                matrixStack.popPose();
            }

            if(!tile.getTank().isEmpty()) {
                FluidStack fluid = tile.getTank().getFluid();
                int combinedLightCorrected = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().offset(Direction.UP.getNormal()));
                RenderHelpers.renderFluidContext(fluid, matrixStack, () -> {
                    float height = Math.max(0.0625F - OFFSET, fluid.getAmount() * 0.0625F / FluidHelpers.BUCKET_VOLUME + 0.0625F - OFFSET);
                    int brightness = Math.max(combinedLightCorrected, fluid.getFluid().getFluidType().getLightLevel(fluid));
                    int l2 = brightness >> 0x10 & 0xFFFF;
                    int i3 = brightness & 0xFFFF;

                    for(Direction side : DirectionHelpers.DIRECTIONS) {
                        TextureAtlasSprite icon = RenderHelpers.getFluidIcon(fluid, Direction.UP);
                        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid.getFluid());
                        Triple<Float, Float, Float> color = Helpers.intToRGB(renderProperties.getTintColor(fluid));

                        VertexConsumer vb = renderTypeBuffer.getBuffer(RenderType.text(icon.atlasLocation()));
                        Matrix4f matrix = matrixStack.last().pose();

                        float[][] c = coordinates[side.ordinal()];
                        float replacedMaxV = (side == Direction.UP || side == Direction.DOWN) ?
                                icon.getV1() : ((icon.getV1() - icon.getV0()) * height + icon.getV0());
                        vb.vertex(matrix, c[0][0], getHeight(side, c[0][1], height), c[0][2]).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU0(), replacedMaxV).uv2(l2, i3).endVertex();
                        vb.vertex(matrix, c[1][0], getHeight(side, c[1][1], height), c[1][2]).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU0(), icon.getV0()).uv2(l2, i3).endVertex();
                        vb.vertex(matrix, c[2][0], getHeight(side, c[2][1], height), c[2][2]).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU1(), icon.getV0()).uv2(l2, i3).endVertex();
                        vb.vertex(matrix, c[3][0], getHeight(side, c[3][1], height), c[3][2]).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).uv(icon.getU1(), replacedMaxV).uv2(l2, i3).endVertex();
                    }
                });
            }
        }
    }

    private void renderItem(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, ItemStack itemStack, BlockEntitySqueezer tile) {
        matrixStack.pushPose();
        float yTop = (9 - tile.getItemHeight()) * 0.125F;
        matrixStack.translate(1F, (yTop - 1F) / 2 + 1F, 1F);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0);
        if (model.isGui3d()) {
            matrixStack.scale(1.7F, 1.7F, 1.7F);
        }
        matrixStack.scale(1F, yTop - 0.125F, 1F);

        Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer, 0);
        matrixStack.popPose();
    }

    private static float getHeight(Direction side, float height, float replaceHeight) {
        if(height == MAXY) {
            return replaceHeight;
        }
        return height;
    }

}
