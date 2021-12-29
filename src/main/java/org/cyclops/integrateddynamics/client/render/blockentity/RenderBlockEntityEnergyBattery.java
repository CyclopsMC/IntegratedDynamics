package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;

/**
 * Renderer for rendering the energy overlay on the {@link org.cyclops.integrateddynamics.block.BlockEnergyBattery}.
 * 
 * @author rubensworks
 *
 */
public class RenderBlockEntityEnergyBattery implements BlockEntityRenderer<BlockEntityEnergyBattery> {

    private static final float OFFSET = 0.001F;
    private static final float MINY = 0F;
    private static final float MAXY = 1F;
    private static final float MIN = 0F - OFFSET;
    private static final float MAX = 1F + OFFSET;
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
                    {MAX, MINY, MAX},
                    {MAX, MAXY, MAX},
                    {MIN, MAXY, MAX},
                    {MIN, MINY, MAX}
            },
            { // WEST
                    {MIN, MINY, MAX},
                    {MIN, MAXY, MAX},
                    {MIN, MAXY, MIN},
                    {MIN, MINY, MIN}
            },
            { // EAST
                    {MAX, MINY, MIN},
                    {MAX, MAXY, MIN},
                    {MAX, MAXY, MAX},
                    {MAX, MINY, MAX}
            }
    };

    public RenderBlockEntityEnergyBattery(BlockEntityRendererProvider.Context context) {

    }

    @Override
	public void render(BlockEntityEnergyBattery tile, float partialTicks, PoseStack matrixStack,
                       MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        if(tile != null && tile.getEnergyStored() > 0) {
            float height = (float) tile.getEnergyStored() / tile.getMaxEnergyStored();

            // Re-scale height to [0.125, 0.875] range as the energy bar does not take up 100% of the height.
            height = (height * 12 / 16) + 0.125F;

            matrixStack.pushPose();

            for(Direction side : Direction.Plane.HORIZONTAL) {
                combinedLight = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().offset(side.getNormal()));
                TextureAtlasSprite icon = RegistryEntries.BLOCK_ENERGY_BATTERY.iconOverlay;

                float[][] c = coordinates[side.ordinal()];
                float replacedMaxV = icon.getV1();
                float replacedMinV = (icon.getV0() - icon.getV1()) * height + icon.getV1();

                float r = 1.0F;
                float g = 1.0F;
                float b = 1.0F;
                if (tile.isCreative()) {
                    float tickFactor = (((float) tile.getLevel().getGameTime() % 20) / 10);
                    if (tickFactor > 1) {
                        tickFactor = -tickFactor + 1;
                    }
                    r = 0.8F + 0.2F * tickFactor;
                    g = 0.42F;
                    b = 0.60F + 0.40F * tickFactor;
                }

                VertexConsumer vb = renderTypeBuffer.getBuffer(RenderType.text(icon.atlas().location()));
                Matrix4f matrix = matrixStack.last().pose();
                vb.vertex(matrix, c[0][0], c[0][1] * height, c[0][2]).color(r, g, b, 1).uv(icon.getU0(), replacedMaxV).uv2(combinedLight).endVertex();
                vb.vertex(matrix, c[1][0], c[1][1] * height, c[1][2]).color(r, g, b, 1).uv(icon.getU0(), replacedMinV).uv2(combinedLight).endVertex();
                vb.vertex(matrix, c[2][0], c[2][1] * height, c[2][2]).color(r, g, b, 1).uv(icon.getU1(), replacedMinV).uv2(combinedLight).endVertex();
                vb.vertex(matrix, c[3][0], c[3][1] * height, c[3][2]).color(r, g, b, 1).uv(icon.getU1(), replacedMaxV).uv2(combinedLight).endVertex();
            }

            matrixStack.popPose();
        }
	}

}
