package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

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
    public void submit(@Nullable ItemStack itemStack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        BlockEntityEnergyBattery tile = new BlockEntityEnergyBattery(BlockPos.ZERO, RegistryEntries.BLOCK_ENERGY_BATTERY.get().defaultBlockState());
        tile.setLevel(Minecraft.getInstance().level);
        BlockEnergyBatteryBase.itemStackToTile(itemStack, tile);
        BlockEntityRenderer<BlockEntityEnergyBattery, BlockEntityRenderState> renderer = this.blockEntityRenderDispatcher.getRenderer(tile);
        BlockEntityRenderState renderState = renderer.createRenderState();
        renderer.extractRenderState(tile, renderState, 0, Vec3.ZERO, null);
        this.blockEntityRenderDispatcher.submit(renderState, poseStack, submitNodeCollector, new CameraRenderState());
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {

    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked<ItemStack> {
        public static final MapCodec<ItemStackBlockEntityEnergyBatteryRender.Unbaked> MAP_CODEC = MapCodec.unit(ItemStackBlockEntityEnergyBatteryRender.Unbaked::new);

        @Override
        public MapCodec<ItemStackBlockEntityEnergyBatteryRender.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<ItemStack> bake(BakingContext bakingContext) {
            return new ItemStackBlockEntityEnergyBatteryRender();
        }
    }

}
