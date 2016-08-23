package org.cyclops.integrateddynamics.client.render.tileentity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.tileentity.TileDryingBasin;
import org.lwjgl.opengl.GL11;

/**
 * Renderer for the item inside the {@link org.cyclops.integrateddynamics.block.BlockDryingBasin}.
 * 
 * @author rubensworks
 *
 */
public class RenderTileEntityDryingBasin extends TileEntitySpecialRenderer<TileDryingBasin> implements RenderHelpers.IFluidContextRender {

    private TileDryingBasin lastTile;

	@Override
	public void renderTileEntityAt(TileDryingBasin tile, double x, double y, double z, float partialTickTime, int partialDamage) {
        if(tile != null) {
            if(tile.getStackInSlot(0) != null) {
                GlStateManager.pushMatrix();
                float var10 = (float) (x - 0.5F);
                float var11 = (float) (y - 0.5F);
                float var12 = (float) (z - 0.5F);
                GlStateManager.translate(var10, var11, var12);
                renderItem(tile.getWorld(), tile.getStackInSlot(0), tile.getRandomRotation());
                GlStateManager.popMatrix();
            }

            lastTile = tile;
            RenderHelpers.renderTileFluidContext(tile.getTank().getFluid(), x, y, z, tile, this);
        }
	}
	
	private void renderItem(World world, ItemStack itemStack, float rotation) {
        GlStateManager.pushMatrix();
        if (itemStack.getItem() instanceof ItemBlock) {
            GlStateManager.translate(1F, 1.2F, 1F);
            GlStateManager.scale(0.6F, 0.6F, 0.6F);
        } else {
            GlStateManager.translate(1F, 1.2F, 1F);
            GlStateManager.rotate(25F, 1, 0, 0);
            GlStateManager.rotate(25F, 0, 1, 0);
            GlStateManager.rotate(rotation, 0, 1, 0);
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
        double height = (fluid.amount * 0.7D) / Fluid.BUCKET_VOLUME + 0.23D + 0.01D;
        int brightness = lastTile.getWorld().getCombinedLight(lastTile.getPos(), fluid.getFluid().getLuminosity(fluid));
        int l2 = brightness >> 0x10 & 0xFFFF;
        int i3 = brightness & 0xFFFF;

        TextureAtlasSprite icon = RenderHelpers.getFluidIcon(lastTile.getTank().getFluid(), EnumFacing.UP);

        Tessellator t = Tessellator.getInstance();
        VertexBuffer worldRenderer = t.getBuffer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        worldRenderer.pos(0.0625F, height, 0.0625F).tex(icon.getMinU(), icon.getMaxV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
        worldRenderer.pos(0.0625F, height, 0.9375F).tex(icon.getMinU(), icon.getMinV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
        worldRenderer.pos(0.9375F, height, 0.9375F).tex(icon.getMaxU(), icon.getMinV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();
        worldRenderer.pos(0.9375F, height, 0.0625F).tex(icon.getMaxU(), icon.getMaxV()).lightmap(l2, i3).color(1F, 1, 1, 1).endVertex();

        t.draw();
    }

}
