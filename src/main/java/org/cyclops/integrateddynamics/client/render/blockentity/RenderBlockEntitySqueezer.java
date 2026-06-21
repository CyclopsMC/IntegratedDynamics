package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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
import org.cyclops.cyclopscore.helper.DirectionHelpers;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.integrateddynamics.blockentity.BlockEntitySqueezer;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer for the item inside the {@link org.cyclops.integrateddynamics.block.BlockDryingBasin}.
 *
 * @author rubensworks
 *
 */
public class RenderBlockEntitySqueezer implements BlockEntityRenderer<BlockEntitySqueezer, RenderBlockEntitySqueezer.RenderState> {

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

    public RenderBlockEntitySqueezer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(BlockEntitySqueezer blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.itemStack = blockEntity.getInventory().getItem(0);
        renderState.fluidStack = blockEntity.getTank().getFluid();
        renderState.level = blockEntity.getLevel();
        renderState.itemHeight = blockEntity.getItemHeight();
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if(!renderState.itemStack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            renderItem(poseStack, submitNodeCollector, renderState);
            poseStack.popPose();
        }

        if(!renderState.fluidStack.isEmpty()) {
            FluidStack fluid = renderState.fluidStack;
            int combinedLightCorrected = LevelRenderer.getLightCoords(renderState.level, renderState.blockPos.offset(Direction.UP.getUnitVec3i()));
            IModHelpersNeoForge.get().getRenderHelpers().renderFluidContext(fluid, poseStack, () -> {
                float height = Math.max(0.0625F - OFFSET, fluid.getAmount() * 0.0625F / IModHelpersNeoForge.get().getFluidHelpers().getBucketVolume() + 0.0625F - OFFSET);
                int brightness = Math.max(combinedLightCorrected, fluid.getFluid().getFluidType().getLightLevel(fluid));
                int l2 = brightness >> 0x10 & 0xFFFF;
                int i3 = brightness & 0xFFFF;

                for(Direction side : DirectionHelpers.DIRECTIONS) {
                    TextureAtlasSprite icon = IModHelpersNeoForge.get().getRenderHelpers().getFluidIcon(fluid, Direction.UP);
                    var fluidTintSource = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState()).fluidTintSource();
                    Triple<Float, Float, Float> color = IModHelpers.get().getBaseHelpers().intToRGB(fluidTintSource != null ? fluidTintSource.colorAsStack(fluid) : 0xFFFFFFFF);

                    submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(icon.atlasLocation()), (pose, vb) -> {
                        float[][] c = coordinates[side.ordinal()];
                        float replacedMaxV = (side == Direction.UP || side == Direction.DOWN) ?
                                icon.getV1() : ((icon.getV1() - icon.getV0()) * height + icon.getV0());
                        vb.addVertex(pose, c[0][0], getHeight(side, c[0][1], height), c[0][2]).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), replacedMaxV).setUv2(l2, i3);
                        vb.addVertex(pose, c[1][0], getHeight(side, c[1][1], height), c[1][2]).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU0(), icon.getV0()).setUv2(l2, i3);
                        vb.addVertex(pose, c[2][0], getHeight(side, c[2][1], height), c[2][2]).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), icon.getV0()).setUv2(l2, i3);
                        vb.addVertex(pose, c[3][0], getHeight(side, c[3][1], height), c[3][2]).setColor(color.getLeft(), color.getMiddle(), color.getRight(), 1).setUv(icon.getU1(), replacedMaxV).setUv2(l2, i3);
                    });
                }
            });
        }
    }

    private void renderItem(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderState renderStateSqueezer) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, renderStateSqueezer.itemStack, ItemDisplayContext.FIXED, renderStateSqueezer.level, null, 0);

        poseStack.pushPose();
        float yTop = (9 - renderStateSqueezer.itemHeight) * 0.125F;
        poseStack.translate(1F, (yTop - 1F) / 2 + 1F, 1F);
        if (renderState.getModelBoundingBox().maxY < 0.5F) {
            float scale = 1.2F + ((float) renderState.getModelBoundingBox().maxY) / 0.5F;
            poseStack.scale(scale, scale, scale);
        }
        poseStack.scale(1F, yTop - 0.125F, 1F);

        renderState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static float getHeight(Direction side, float height, float replaceHeight) {
        if(height == MAXY) {
            return replaceHeight;
        }
        return height;
    }

    public static class RenderState extends BlockEntityRenderState {
        public ItemStack itemStack;
        public FluidStack fluidStack;
        public Level level;
        public int itemHeight;
    }

}
