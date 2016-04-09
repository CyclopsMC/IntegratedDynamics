package org.cyclops.integrateddynamics.client.render.tileentity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.DirectionHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;
import org.lwjgl.opengl.GL11;

/**
 * Renderer for the item inside the {@link org.cyclops.integrateddynamics.block.BlockDryingBasin}.
 * 
 * @author rubensworks
 *
 */
public class RenderTileEntitySqueezer extends TileEntitySpecialRenderer<TileSqueezer> implements RenderHelpers.IFluidContextRender {

    private static final double OFFSET = 0.01D;
    private static final double MINY = 0.0625D;
    private static final double MAXY = 0.125D - OFFSET;
    private static final double MIN = 0D + OFFSET;
    private static final double MAX = 1D - OFFSET;
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

    private TileSqueezer lastTile;

	@Override
	public void renderTileEntityAt(TileSqueezer tile, double x, double y, double z, float partialTickTime, int partialDamage) {
        if(tile != null) {
            if(tile.getStackInSlot(0) != null) {
                GlStateManager.pushMatrix();
                float var10 = (float) (x - 0.5F);
                float var11 = (float) (y - 0.5F);
                float var12 = (float) (z - 0.5F);
                GlStateManager.translate(var10, var11, var12);
                renderItem(tile.getWorld(), tile.getPos(), tile.getStackInSlot(0), tile);
                GlStateManager.popMatrix();
            }

            if(!tile.getTank().isEmpty()) {
                lastTile = tile;
                RenderHelpers.renderTileFluidContext(tile.getTank().getFluid(), x, y, z, tile, this);
            }
        }
	}
	
	private void renderItem(World world, BlockPos pos, ItemStack itemStack, TileSqueezer tile) {
        GlStateManager.pushMatrix();
        float yTop = (9 - tile.getItemHeight()) * 0.125F;
        GlStateManager.translate(1F, (yTop - 1F) / 2 + 1F, 1F);
        GlStateManager.scale(0.7F, 0.7F, 0.7F);
        GlStateManager.scale(1F, yTop - 0.125F, 1F);
        
        GlStateManager.pushAttrib();
        RenderHelper.enableStandardItemLighting();
        RenderHelpers.renderItem(itemStack);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popAttrib();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public void renderFluid(FluidStack fluid) {
        double height = Math.max(0.0625D - OFFSET, ((double) fluid.amount) * 0.0625D / FluidContainerRegistry.BUCKET_VOLUME + 0.0625D - OFFSET);
        int brightness = lastTile.getWorld().getCombinedLight(lastTile.getPos(), fluid.getFluid().getLuminosity(fluid));
        int l2 = brightness >> 0x10 & 0xFFFF;
        int i3 = brightness & 0xFFFF;

        for(EnumFacing side : DirectionHelpers.DIRECTIONS) {
            TextureAtlasSprite icon = RenderHelpers.getFluidIcon(lastTile.getTank().getFluid(), EnumFacing.UP);

            Tessellator t = Tessellator.getInstance();
            VertexBuffer worldRenderer = t.getBuffer();
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

            double[][] c = coordinates[side.ordinal()];
            double replacedMaxV = (side == EnumFacing.UP || side == EnumFacing.DOWN) ?
                    icon.getMaxV() : ((icon.getMaxV() - icon.getMinV()) * height + icon.getMinV());
            worldRenderer.pos(c[0][0], getHeight(side, c[0][1], height), c[0][2]).tex(icon.getMinU(), replacedMaxV).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
            worldRenderer.pos(c[1][0], getHeight(side, c[1][1], height), c[1][2]).tex(icon.getMinU(), icon.getMinV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
            worldRenderer.pos(c[2][0], getHeight(side, c[2][1], height), c[2][2]).tex(icon.getMaxU(), icon.getMinV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
            worldRenderer.pos(c[3][0], getHeight(side, c[3][1], height), c[3][2]).tex(icon.getMaxU(), replacedMaxV).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();

            t.draw();
        }
    }

    private static double getHeight(EnumFacing side, double height, double replaceHeight) {
        if(height == MAXY) {
            return replaceHeight;
        }
        return height;
    }

}
