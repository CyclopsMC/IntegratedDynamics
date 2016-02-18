package org.cyclops.integrateddynamics.client.render.tileentity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
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

            lastTile = tile;
            RenderHelpers.renderTileFluidContext(tile.getTank().getFluid(), x, y, z, tile, this);
        }
	}
	
	private void renderItem(World world, BlockPos pos, ItemStack itemStack, TileSqueezer tile) {
        GlStateManager.pushMatrix();
        if (itemStack.getItem() instanceof ItemBlock) {
            float yTop = (9 - tile.getItemHeight()) * 0.125F;
            GlStateManager.translate(1F, (yTop - 1F) / 2 + 1F, 1F);
            GlStateManager.scale(1.4F, 1.4F, 1.4F);
            GlStateManager.scale(1F, yTop - 0.125F, 1F);
        } else {
            // TODO: item scaling like blocks
            GlStateManager.translate(1F, 1.2F, 1F);
            GlStateManager.rotate(25F, 1, 0, 0);
            GlStateManager.rotate(25F, 0, 1, 0);
        }
        
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
        // TODO: render in basin and 2 sides
        double height = fluid.amount * 0.90D / FluidContainerRegistry.BUCKET_VOLUME;
        int brightness = lastTile.getWorld().getCombinedLight(lastTile.getPos(), fluid.getFluid().getLuminosity(fluid));
        int l2 = brightness >> 0x10 & 0xFFFF;
        int i3 = brightness & 0xFFFF;

        TextureAtlasSprite icon = RenderHelpers.getFluidIcon(lastTile.getTank().getFluid(), EnumFacing.UP);

        Tessellator t = Tessellator.getInstance();
        WorldRenderer worldRenderer = t.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        worldRenderer.pos(0, height, 0).tex(icon.getMinU(), icon.getMaxV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
        worldRenderer.pos(0, height, 1).tex(icon.getMinU(), icon.getMinV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
        worldRenderer.pos(1, height, 1).tex(icon.getMaxU(), icon.getMinV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
        worldRenderer.pos(1, height, 0).tex(icon.getMaxU(), icon.getMaxV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();

        t.draw();
    }

}
