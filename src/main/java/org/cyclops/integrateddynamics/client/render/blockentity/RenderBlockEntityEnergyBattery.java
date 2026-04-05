package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBatteryConfigClient;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer for rendering the energy overlay on the {@link org.cyclops.integrateddynamics.block.BlockEnergyBattery}.
 *
 * @author rubensworks
 *
 */
public class RenderBlockEntityEnergyBattery implements BlockEntityRenderer<BlockEntityEnergyBattery, RenderBlockEntityEnergyBattery.RenderState> {

    private static final float OFFSET = 0.001F;
    private static final float MINY = 0F;
    private static final float MAXY = 1F;
    private static final float MIN = 0F - OFFSET;
    private static final float MAX = 1F + OFFSET;
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
                    {MAX, MINY, MAX},
                    {MAX, MAXY, MAX},
                    {MIN, MAXY, MAX},
                    {MIN, MINY, MAX}
            },
            { // WEST
                    {MIN, MINY, MAX},
                    {MIN, MAXY, MAX},
                    {MIN, MAXY, MIN},
                    {MIN, MINY, MIN}
            },
            { // EAST
                    {MAX, MINY, MIN},
                    {MAX, MAXY, MIN},
                    {MAX, MAXY, MAX},
                    {MAX, MINY, MAX}
            }
    };

    public RenderBlockEntityEnergyBattery(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public boolean shouldRender(BlockEntityEnergyBattery blockEntity, Vec3 cameraPos) {
        return blockEntity.getBlockPos() == BlockPos.ZERO || BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEnergyBattery blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.energyStored = blockEntity.getEnergyStored();
        renderState.maxEnergyStored = blockEntity.getMaxEnergyStored();
        renderState.combinedLights = new EnumFacingMap<>();
        for(Direction side : Direction.Plane.HORIZONTAL) {
            renderState.combinedLights.put(side, LevelRenderer.getLightCoords(blockEntity.getLevel(), blockEntity.getBlockPos().offset(side.getUnitVec3i())));
        }
        renderState.creative = blockEntity.isCreative();
        renderState.gameTime = blockEntity.getLevel().getGameTime();
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if(renderState.energyStored > 0) {
            float heightBase = Math.min(1.0F, (float) renderState.energyStored / renderState.maxEnergyStored);
            // Re-scale height to [0.125, 0.875] range as the energy bar does not take up 100% of the height.
            float height = (heightBase * 12 / 16) + 0.125F;

            poseStack.pushPose();

            for(Direction side : Direction.Plane.HORIZONTAL) {
                int combinedLight = renderState.combinedLights.get(side);
                TextureAtlasSprite icon = BlockEntityEnergyBatteryConfigClient.ICON_OVERLAY;

                float[][] c = coordinates[side.ordinal()];
                float replacedMaxV = icon.getV1();
                float replacedMinV = (icon.getV0() - icon.getV1()) * height + icon.getV1();

                float r;
                float g;
                float b;
                if (renderState.creative) {
                    float tickFactor = (((float) renderState.gameTime % 20) / 10);
                    if (tickFactor > 1) {
                        tickFactor = -tickFactor + 1;
                    }
                    r = 0.8F + 0.2F * tickFactor;
                    g = 0.42F;
                    b = 0.60F + 0.40F * tickFactor;
                } else {
                    b = 1.0F;
                    g = 1.0F;
                    r = 1.0F;
                }

                submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(icon.atlasLocation()), (pose, vb) -> {
                    vb.addVertex(pose, c[0][0], c[0][1] * height, c[0][2]).setColor(r, g, b, 1).setUv(icon.getU0(), replacedMaxV).setLight(combinedLight);
                    vb.addVertex(pose, c[1][0], c[1][1] * height, c[1][2]).setColor(r, g, b, 1).setUv(icon.getU0(), replacedMinV).setLight(combinedLight);
                    vb.addVertex(pose, c[2][0], c[2][1] * height, c[2][2]).setColor(r, g, b, 1).setUv(icon.getU1(), replacedMinV).setLight(combinedLight);
                    vb.addVertex(pose, c[3][0], c[3][1] * height, c[3][2]).setColor(r, g, b, 1).setUv(icon.getU1(), replacedMaxV).setLight(combinedLight);
                });
            }

            poseStack.popPose();
        }
    }

    public static class RenderState extends BlockEntityRenderState {
        public int energyStored;
        public int maxEnergyStored;
        public EnumFacingMap<Integer> combinedLights;
        public boolean creative;
        public long gameTime;
    }

}
