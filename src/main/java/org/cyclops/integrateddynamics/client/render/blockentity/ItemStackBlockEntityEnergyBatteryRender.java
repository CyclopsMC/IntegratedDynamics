package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ItemStackBlockEntityEnergyBatteryRender extends BlockEntityWithoutLevelRenderer {

    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public ItemStackBlockEntityEnergyBatteryRender() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        BlockEntityEnergyBattery tile = new BlockEntityEnergyBattery(BlockPos.ZERO, RegistryEntries.BLOCK_ENERGY_BATTERY.get().defaultBlockState());
        BlockEnergyBatteryBase.itemStackToTile(itemStackIn, tile);
        this.blockEntityRenderDispatcher.renderItem(tile, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

}
