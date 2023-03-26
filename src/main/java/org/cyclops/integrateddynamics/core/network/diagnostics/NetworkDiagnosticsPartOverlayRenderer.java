package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import java.util.List;
import java.util.Set;

/**
 * @author rubensworks
 */
public class NetworkDiagnosticsPartOverlayRenderer {

    private static final NetworkDiagnosticsPartOverlayRenderer _INSTANCE = new NetworkDiagnosticsPartOverlayRenderer();
    private final Set<PartPos> partPositions = Sets.newHashSet();

    private NetworkDiagnosticsPartOverlayRenderer() {

    }

    public static NetworkDiagnosticsPartOverlayRenderer getInstance() {
        return _INSTANCE;
    }

    public synchronized void addPos(PartPos pos) {
        partPositions.add(pos);
    }

    public synchronized void removePos(PartPos pos) {
        partPositions.remove(pos);
    }

    public synchronized void clearPositions() {
        partPositions.clear();
    }

    public synchronized boolean hasPartPos(PartPos pos) {
        return partPositions.contains(pos);
    }

    @SubscribeEvent
    public void onRender(RenderLevelStageEvent event) {
        if (!partPositions.isEmpty() && event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            Player player = Minecraft.getInstance().player;
            float partialTicks = event.getPartialTick();

            Vec3 eyePos = player.getEyePosition(partialTicks);
            double offsetX = eyePos.x;
            double offsetY = eyePos.y;
            double offsetZ = eyePos.z;

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.lineWidth(6.0F);
            RenderSystem.depthMask(false);

            List<PartPos> partList = Lists.newArrayList(partPositions);
            for (PartPos partPos : partList) {
                if (partPos.getPos().getLevelKey().location().equals(player.level.dimension().location()) && partPos.getPos().getBlockPos().distSqr(player.blockPosition()) < 10000) {
                    PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
                    final VoxelShape shape;
                    if (partStateHolder != null) {
                        shape = partStateHolder.getPart().getPartRenderPosition().getBoundingBox(partPos.getSide());
                    } else {
                        shape = Shapes.BLOCK;
                    }

                    AABB bb = shape
                            .bounds()
                            .move(partPos.getPos().getBlockPos())
                            .move(-offsetX, -offsetY, -offsetZ)
                            .inflate(0.05, 0.05, 0.05)
                            .inflate(-0.05, -0.05, -0.05);
                    LevelRenderer.renderLineBox(event.getPoseStack(), Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.lines()),
                            bb, 1.0F, 0.2F, 0.1F, 0.8F);
                }
            }

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    }

}
