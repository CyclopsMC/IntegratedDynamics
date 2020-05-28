package org.cyclops.integrateddynamics.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
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
import org.cyclops.cyclopscore.helper.DirectionHelpers;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;

/**
 * Renderer for the item inside the {@link org.cyclops.integrateddynamics.block.BlockDryingBasin}.
 * 
 * @author rubensworks
 *
 */
public class RenderTileEntitySqueezer extends TileEntityRenderer<TileSqueezer> {

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

    public RenderTileEntitySqueezer(TileEntityRendererDispatcher tileEntityRendererDispatcher) {
        super(tileEntityRendererDispatcher);
    }

    @Override
	public void render(TileSqueezer tile, float partialTicks, MatrixStack matrixStack,
                       IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay) {
        if(tile != null) {
            if(!tile.getInventory().getStackInSlot(0).isEmpty()) {
                matrixStack.push();
                matrixStack.translate(-0.5F, -0.5F, -0.5F);
                renderItem(matrixStack, renderTypeBuffer, tile.getInventory().getStackInSlot(0), tile);
                matrixStack.pop();
            }

            if(!tile.getTank().isEmpty()) {
                FluidStack fluid = tile.getTank().getFluid();
                int combinedLightCorrected = WorldRenderer.getCombinedLight(tile.getWorld(), tile.getPos().add(Direction.UP.getDirectionVec()));
                RenderHelpers.renderFluidContext(fluid, matrixStack, () -> {
                    float height = Math.max(0.0625F - OFFSET, fluid.getAmount() * 0.0625F / FluidHelpers.BUCKET_VOLUME + 0.0625F - OFFSET);
                    int brightness = Math.max(combinedLightCorrected, fluid.getFluid().getAttributes().getLuminosity(fluid));
                    int l2 = brightness >> 0x10 & 0xFFFF;
                    int i3 = brightness & 0xFFFF;

                    for(Direction side : DirectionHelpers.DIRECTIONS) {
                        TextureAtlasSprite icon = RenderHelpers.getFluidIcon(fluid, Direction.UP);
                        Triple<Float, Float, Float> color = Helpers.intToRGB(fluid.getFluid().getAttributes().getColor(tile.getWorld(), tile.getPos()));

                        IVertexBuilder vb = renderTypeBuffer.getBuffer(RenderType.getText(icon.getAtlasTexture().getTextureLocation()));
                        Matrix4f matrix = matrixStack.getLast().getMatrix();

                        float[][] c = coordinates[side.ordinal()];
                        float replacedMaxV = (side == Direction.UP || side == Direction.DOWN) ?
                                icon.getMaxV() : ((icon.getMaxV() - icon.getMinV()) * height + icon.getMinV());
                        vb.pos(matrix, c[0][0], getHeight(side, c[0][1], height), c[0][2]).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMinU(), replacedMaxV).lightmap(l2, i3).endVertex();
                        vb.pos(matrix, c[1][0], getHeight(side, c[1][1], height), c[1][2]).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMinU(), icon.getMinV()).lightmap(l2, i3).endVertex();
                        vb.pos(matrix, c[2][0], getHeight(side, c[2][1], height), c[2][2]).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMaxU(), icon.getMinV()).lightmap(l2, i3).endVertex();
                        vb.pos(matrix, c[3][0], getHeight(side, c[3][1], height), c[3][2]).color(color.getLeft(), color.getMiddle(), color.getRight(), 1).tex(icon.getMaxU(), replacedMaxV).lightmap(l2, i3).endVertex();
                    }
                });
            }
        }
	}
	
	private void renderItem(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, ItemStack itemStack, TileSqueezer tile) {
        matrixStack.push();
        float yTop = (9 - tile.getItemHeight()) * 0.125F;
        matrixStack.translate(1F, (yTop - 1F) / 2 + 1F, 1F);
        IBakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(itemStack, null, null);
        if (model.isGui3d()) {
            matrixStack.scale(1.7F, 1.7F, 1.7F);
        }
        matrixStack.scale(1F, yTop - 0.125F, 1F);

        Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer);
        matrixStack.pop();
    }

    private static float getHeight(Direction side, float height, float replaceHeight) {
        if(height == MAXY) {
            return replaceHeight;
        }
        return height;
    }

}
