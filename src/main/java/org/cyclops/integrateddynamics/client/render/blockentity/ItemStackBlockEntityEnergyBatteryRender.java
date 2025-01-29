package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.jetbrains.annotations.Nullable;

/**
 * @author rubensworks
 */
public class ItemStackBlockEntityEnergyBatteryRender implements SpecialModelRenderer<ItemStack> {

    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public ItemStackBlockEntityEnergyBatteryRender() {
        blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    @Override
    public @Nullable ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    @Override
    public void render(@Nullable ItemStack itemStackIn, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        BlockEntityEnergyBattery tile = new BlockEntityEnergyBattery(BlockPos.ZERO, RegistryEntries.BLOCK_ENERGY_BATTERY.get().defaultBlockState());
        tile.setLevel(Minecraft.getInstance().level);
        BlockEnergyBatteryBase.itemStackToTile(itemStackIn, tile);
        this.blockEntityRenderDispatcher.render(tile, 0, poseStack, bufferSource);
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<ItemStackBlockEntityEnergyBatteryRender.Unbaked> MAP_CODEC = MapCodec.unit(ItemStackBlockEntityEnergyBatteryRender.Unbaked::new);

        @Override
        public MapCodec<ItemStackBlockEntityEnergyBatteryRender.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new ItemStackBlockEntityEnergyBatteryRender();
        }
    }

}
