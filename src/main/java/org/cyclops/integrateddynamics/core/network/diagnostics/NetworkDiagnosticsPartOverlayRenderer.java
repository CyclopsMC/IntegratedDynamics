package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
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
    public void onRender(RenderLevelStageEvent.AfterSky event) {
        if (!partPositions.isEmpty()) {
            Player player = Minecraft.getInstance().player;

            Vec3 eyePos = event.getLevelRenderState().cameraRenderState.pos;
            double offsetX = eyePos.x;
            double offsetY = eyePos.y;
            double offsetZ = eyePos.z;

            RenderSystem.lineWidth(6.0F);

            List<PartPos> partList = Lists.newArrayList(partPositions);
            for (PartPos partPos : partList) {
                if (partPos.getPos().getLevelKey().location().equals(player.level().dimension().location()) && partPos.getPos().getBlockPos().distSqr(player.blockPosition()) < 10000) {
                    PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
                    final VoxelShape shape;
                    if (partStateHolder != null) {
                        shape = partStateHolder.getPart().getPartRenderPosition().getBoundingBox(partPos.getSide(), CollisionContext.empty());
                    } else {
                        shape = Shapes.BLOCK;
                    }

                    AABB bb = shape
                            .bounds()
                            .move(partPos.getPos().getBlockPos())
                            .move(-offsetX, -offsetY, -offsetZ)
                            .inflate(0.05, 0.05, 0.05)
                            .inflate(-0.05, -0.05, -0.05);
                    ShapeRenderer.renderLineBox(event.getPoseStack().last(), Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.lines()),
                            bb, 1.0F, 0.2F, 0.1F, 0.8F);
                }
            }
        }
    }

}
