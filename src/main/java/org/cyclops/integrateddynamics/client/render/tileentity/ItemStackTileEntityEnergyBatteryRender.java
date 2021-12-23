package org.cyclops.integrateddynamics.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ItemStackTileEntityEnergyBatteryRender extends ItemStackTileEntityRenderer {

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        TileEnergyBattery tile = new TileEnergyBattery();
        BlockEnergyBatteryBase.itemStackToTile(itemStackIn, tile);
        TileEntityRendererDispatcher.instance.renderItem(tile, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

}
