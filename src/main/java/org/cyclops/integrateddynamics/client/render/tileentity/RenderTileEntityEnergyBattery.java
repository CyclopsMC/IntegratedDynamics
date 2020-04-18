package org.cyclops.integrateddynamics.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

/**
 * Renderer for rendering the energy overlay on the {@link org.cyclops.integrateddynamics.block.BlockEnergyBattery}.
 * 
 * @author rubensworks
 *
 */
public class RenderTileEntityEnergyBattery extends TileEntityRenderer<TileEnergyBattery> {

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

    public RenderTileEntityEnergyBattery(TileEntityRendererDispatcher tileEntityRendererDispatcher) {
        super(tileEntityRendererDispatcher);
    }

    @Override
	public void render(TileEnergyBattery tile, float partialTicks, MatrixStack matrixStack,
                       IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay) {
        if(tile != null && tile.getEnergyStored() > 0) {
            float height = tile.getEnergyStored() / tile.getMaxEnergyStored();

            // Re-scale height to [0.125, 0.875] range as the energy bar does not take up 100% of the height.
            height = (height * 12 / 16) + 0.125F;

            int l2 = combinedLight >> 0x10 & 0xFFFF;
            int i3 = combinedLight & 0xFFFF;

            matrixStack.push();

            for(Direction side : Direction.Plane.HORIZONTAL) {
                TextureAtlasSprite icon = RegistryEntries.BLOCK_ENERGY_BATTERY.iconOverlay;

                float[][] c = coordinates[side.ordinal()];
                float replacedMaxV = icon.getMaxV();
                float replacedMinV = (icon.getMinV() - icon.getMaxV()) * height + icon.getMaxV();

                float r = 1.0F;
                float g = 1.0F;
                float b = 1.0F;
                if (tile.isCreative()) {
                    float tickFactor = (((float) tile.getWorld().getGameTime() % 20) / 10);
                    if (tickFactor > 1) {
                        tickFactor = -tickFactor + 1;
                    }
                    r = 0.8F + 0.2F * tickFactor;
                    g = 0.42F;
                    b = 0.60F + 0.40F * tickFactor;
                }

                IVertexBuilder vb = renderTypeBuffer.getBuffer(RenderType.getText(icon.getAtlasTexture().getTextureLocation()));
                Matrix4f matrix = matrixStack.getLast().getMatrix();
                vb.pos(matrix, c[0][0], c[0][1] * height, c[0][2]).color(r, g, b, 1).tex(icon.getMinU(), replacedMaxV).lightmap(l2, i3).endVertex();
                vb.pos(matrix, c[1][0], c[1][1] * height, c[1][2]).color(r, g, b, 1).tex(icon.getMinU(), replacedMinV).lightmap(l2, i3).endVertex();
                vb.pos(matrix, c[2][0], c[2][1] * height, c[2][2]).color(r, g, b, 1).tex(icon.getMaxU(), replacedMinV).lightmap(l2, i3).endVertex();
                vb.pos(matrix, c[3][0], c[3][1] * height, c[3][2]).color(r, g, b, 1).tex(icon.getMaxU(), replacedMaxV).lightmap(l2, i3).endVertex();
            }

            matrixStack.pop();
        }
	}

}
