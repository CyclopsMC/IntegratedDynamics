package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer for the item inside the {@link org.cyclops.integrateddynamics.block.BlockDryingBasin}.
 *
 * @author rubensworks
 *
 */
public class RenderBlockEntityDryingBasin implements BlockEntityRenderer<BlockEntityDryingBasin, RenderBlockEntityDryingBasin.RenderState> {

    public RenderBlockEntityDryingBasin(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(BlockEntityDryingBasin blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.itemStack = blockEntity.getInventory().getItem(0);
        renderState.fluidStack = blockEntity.getTank().getFluid();
        renderState.rotation = blockEntity.getRandomRotation();
        renderState.level = blockEntity.getLevel();
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if(!renderState.itemStack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            renderItem(poseStack, submitNodeCollector, renderState);
            poseStack.popPose();
        }

        FluidStack fluid = renderState.fluidStack;
        IModHelpersNeoForge.get().getRenderHelpers().renderFluidContext(fluid, poseStack, () -> {
            float height = (float) ((fluid.getAmount() * 0.7D) / IModHelpersNeoForge.get().getFluidHelpers().getBucketVolume() + 0.23D + 0.01D);
            int brightness = Math.max(renderState.lightCoords, fluid.getFluid().getFluidType().getLightLevel(fluid));
            int l2 = brightness >> 0x10 & 0xFFFF;
            int i3 = brightness & 0xFFFF;

            TextureAtlasSprite icon = IModHelpersNeoForge.get().getRenderHelpers().getFluidIcon(fluid, Direction.UP);
            var fluidTintSource = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState()).fluidTintSource();
            Triple<Float, Float, Float> color = IModHelpers.get().getBaseHelpers().intToRGB(fluidTintSource != null ? fluidTintSource.colorAsStack(fluid) : 0xFFFFFFFF);

            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(icon.atlasLocation()), (pose, vb) -> {
                vb.addVertex(pose, 0.0625F, height, 0.0625F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), icon.getV1()).setUv2(l2, i3);
                vb.addVertex(pose, 0.0625F, height, 0.9375F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), icon.getV0()).setUv2(l2, i3);
                vb.addVertex(pose, 0.9375F, height, 0.9375F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), icon.getV0()).setUv2(l2, i3);
                vb.addVertex(pose, 0.9375F, height, 0.0625F).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), icon.getV1()).setUv2(l2, i3);
            });
        });
    }

    private void renderItem(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderState renderStateDryingBasin) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, renderStateDryingBasin.itemStack, ItemDisplayContext.FIXED, renderStateDryingBasin.level, null, 0);

        if (renderState.isOversizedInGui()) {
            poseStack.translate(1F, 1.2F, 1F);
            poseStack.scale(1.2F, 1.2F, 1.2F);
        } else {
            poseStack.translate(1F, 1.2F, 1F);
            poseStack.mulPose(Axis.XP.rotationDegrees(25F));
            poseStack.mulPose(Axis.YP.rotationDegrees(25F));
            poseStack.mulPose(Axis.YP.rotationDegrees(renderStateDryingBasin.rotation));
        }

        renderState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
    }

    public static class RenderState extends BlockEntityRenderState {
        public ItemStack itemStack;
        public FluidStack fluidStack;
        public float rotation;
        public Level level;
    }

}
