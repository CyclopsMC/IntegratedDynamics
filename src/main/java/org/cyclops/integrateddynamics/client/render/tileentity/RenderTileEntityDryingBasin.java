package org.cyclops.integrateddynamics.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.tileentity.TileDryingBasin;

/**
 * Renderer for the item inside the {@link org.cyclops.integrateddynamics.block.BlockDryingBasin}.
 * 
 * @author rubensworks
 *
 */
public class RenderTileEntityDryingBasin extends TileEntityRenderer<TileDryingBasin> {

    public RenderTileEntityDryingBasin(TileEntityRendererDispatcher tileEntityRendererDispatcher) {
        super(tileEntityRendererDispatcher);
    }

    @Override
    public void render(TileDryingBasin tile, float partialTicks, MatrixStack matrixStack,
                       IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay) {
        if(tile != null) {
            if(!tile.getInventory().getStackInSlot(0).isEmpty()) {
                matrixStack.push();
                matrixStack.translate(-0.5F, -0.5F, -0.5F);
                renderItem(matrixStack, renderTypeBuffer, tile.getInventory().getStackInSlot(0), tile.getRandomRotation());
                matrixStack.pop();
            }

            FluidStack fluid = tile.getTank().getFluid();
            RenderHelpers.renderFluidContext(fluid, matrixStack, () -> {
                float height = (float) ((fluid.getAmount() * 0.7D) / FluidHelpers.BUCKET_VOLUME + 0.23D + 0.01D);
                int brightness = Math.max(combinedLight, fluid.getFluid().getAttributes().getLuminosity(fluid));
                int l2 = brightness >> 0x10 & 0xFFFF;
                int i3 = brightness & 0xFFFF;

                TextureAtlasSprite icon = RenderHelpers.getFluidIcon(fluid, Direction.UP);
                Triple<Float, Float, Float> color = Helpers.intToRGB(fluid.getFluid().getAttributes().getColor(tile.getWorld(), tile.getPos()));

                IVertexBuilder vb = renderTypeBuffer.getBuffer(RenderType.getText(icon.getAtlasTexture().getTextureLocation()));
                Matrix4f matrix = matrixStack.getLast().getMatrix();
                vb.pos(matrix, 0.0625F, height, 0.0625F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMinU(), icon.getMaxV()).lightmap(l2, i3).endVertex();
                vb.pos(matrix, 0.0625F, height, 0.9375F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMinU(), icon.getMinV()).lightmap(l2, i3).endVertex();
                vb.pos(matrix, 0.9375F, height, 0.9375F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMaxU(), icon.getMinV()).lightmap(l2, i3).endVertex();
                vb.pos(matrix, 0.9375F, height, 0.0625F).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMaxU(), icon.getMaxV()).lightmap(l2, i3).endVertex();
            });
        }
	}
	
	private void renderItem(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, ItemStack itemStack, float rotation) {
        IBakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(itemStack, null, null);
        if (model.isGui3d()) {
            matrixStack.translate(1F, 1.2F, 1F);
            matrixStack.scale(1.2F, 1.2F, 1.2F);
        } else {
            matrixStack.translate(1F, 1.2F, 1F);
            matrixStack.rotate(Vector3f.XP.rotationDegrees(25F));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(25F));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(rotation));
        }

        Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer);
    }

}
