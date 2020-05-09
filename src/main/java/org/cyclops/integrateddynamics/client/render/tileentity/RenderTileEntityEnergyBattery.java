package org.cyclops.integrateddynamics.client.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;
import org.lwjgl.opengl.GL11;

/**
 * Renderer for rendering the energy overlay on the {@link org.cyclops.integrateddynamics.block.BlockEnergyBattery}.
 * 
 * @author rubensworks
 *
 */
public class RenderTileEntityEnergyBattery extends TileEntitySpecialRenderer<TileEnergyBattery> {

    private static final double OFFSET = 0.001D;
    private static final double MINY = 0D;
    private static final double MAXY = 1D;
    private static final double MIN = 0D - OFFSET;
    private static final double MAX = 1D + OFFSET;
    private static double[][][] coordinates = {
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

	@Override
	public void render(TileEnergyBattery tile, double x, double y, double z, float partialTickTime, int partialDamage, float alpha) {
        if(tile != null && tile.getEnergyStored() > 0) {
            double height = (double) tile.getEnergyStored() / tile.getMaxEnergyStored();

            // Re-scale height to [0.125, 0.875] range as the energy bar does not take up 100% of the height.
            height = (height * 12 / 16) + 0.125D;

            int brightness = tile.getWorld().getCombinedLight(tile.getPos(), 15);
            int l2 = brightness >> 0x10 & 0xFFFF;
            int i3 = brightness & 0xFFFF;

            GlStateManager.pushMatrix();

            // Correct color & lighting
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // Set to current relative player location
            GlStateManager.translate(x, y, z);

            // Set blockState textures
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            for(EnumFacing side : EnumFacing.HORIZONTALS) {
                TextureAtlasSprite icon = BlockEnergyBattery.getInstance().iconOverlay;

                Tessellator t = Tessellator.getInstance();
                BufferBuilder worldRenderer = t.getBuffer();
                worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

                double[][] c = coordinates[side.ordinal()];
                double replacedMaxV = icon.getMaxV();
                double replacedMinV = ((icon.getMinV() - icon.getMaxV()) * height + icon.getMaxV());

                float r = 1.0F;
                float g = 1.0F;
                float b = 1.0F;
                if (tile.isCreative()) {
                    float tickFactor = (((float) tile.getWorld().getTotalWorldTime() % 20) / 10);
                    if (tickFactor > 1) {
                        tickFactor = -tickFactor + 1;
                    }
                    r = 0.8F + 0.2F * tickFactor;
                    g = 0.42F;
                    b = 0.60F + 0.40F * tickFactor;
                }

                worldRenderer.pos(c[0][0], c[0][1] * height, c[0][2]).tex(icon.getMinU(), replacedMaxV).lightmap(l2, i3).color(r, g, b, 1).endVertex();
                worldRenderer.pos(c[1][0], c[1][1] * height, c[1][2]).tex(icon.getMinU(), replacedMinV).lightmap(l2, i3).color(r, g, b, 1).endVertex();
                worldRenderer.pos(c[2][0], c[2][1] * height, c[2][2]).tex(icon.getMaxU(), replacedMinV).lightmap(l2, i3).color(r, g, b, 1).endVertex();
                worldRenderer.pos(c[3][0], c[3][1] * height, c[3][2]).tex(icon.getMaxU(), replacedMaxV).lightmap(l2, i3).color(r, g, b, 1).endVertex();

                t.draw();
            }

            GlStateManager.enableLighting();
            //GlStateManager.disableDepth();
            GlStateManager.popMatrix();
        }
	}

}
