package org.cyclops.integrateddynamics.client.render.level;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.network.PartOffsetsClientNotifier;
import org.cyclops.integrateddynamics.network.packet.PartOffsetsSubscribePacket;

import java.util.List;
import java.util.Random;

/**
 * @author rubensworks
 */
public class PartOffsetsOverlayRenderer {

    private static final PartOffsetsOverlayRenderer _INSTANCE = new PartOffsetsOverlayRenderer();

    private boolean subscribedToServerChanges = false;

    private List<PartOffsetsClientNotifier.Entry> data = Lists.newArrayList();

    private PartOffsetsOverlayRenderer() {

    }

    public static PartOffsetsOverlayRenderer getInstance() {
        return _INSTANCE;
    }

    private void subscribeToServerChanges() {
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new PartOffsetsSubscribePacket(true));
        this.subscribedToServerChanges = true;
    }

    private void unsubscribeToServerChanges() {
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new PartOffsetsSubscribePacket(false));
        this.subscribedToServerChanges = false;
    }

    public void clear() {
        this.data.clear();
    }

    public void setData(List<PartOffsetsClientNotifier.Entry> data) {
        this.data = data;
    }

    @SubscribeEvent
    public void onRender(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        // If the player is holding a wrench, show the offsets of parts
        // Only do this for parts with non-default target side or non-default offset
        Player player = Minecraft.getInstance().player;
        if (player.getItemInHand(InteractionHand.MAIN_HAND).is(WrenchHelpers.TAG_WRENCH)
                || player.getItemInHand(InteractionHand.OFF_HAND).is(WrenchHelpers.TAG_WRENCH)) {
            if (!subscribedToServerChanges) {
                this.subscribeToServerChanges();
            }

            Vec3 eyePos = event.getLevelRenderState().cameraRenderState.pos;
            for (PartOffsetsClientNotifier.Entry entry : this.data) {
                this.renderOffset(entry, eyePos);
            }

        } else if (subscribedToServerChanges) {
            this.data.clear();
            this.unsubscribeToServerChanges();
        }
    }

    private void renderOffset(PartOffsetsClientNotifier.Entry entry, Vec3 eyePos) {
        Random posRand = new Random(entry.source().asLong());
        float r = 0.5F + posRand.nextFloat() / 2;
        float g = 0.5F + posRand.nextFloat() / 2;
        float b = 0.5F + posRand.nextFloat() / 2;
        float a = 0.90F;

        // Draw line from center to target
        Gizmos.line(
                Vec3.atCenterOf(entry.source())
                        .add(
                                entry.sourceSide().getStepX() * 0.5F,
                                entry.sourceSide().getStepY() * 0.5F,
                                entry.sourceSide().getStepZ() * 0.5F
                        ),
                Vec3.atCenterOf(entry.source()
                        .offset(entry.targetOffset()))
                        .add(
                                entry.targetSide().getAxis() != entry.sourceSide().getAxis() ? entry.targetSide().getStepX() * 0.5F : 0,
                                entry.targetSide().getAxis() != entry.sourceSide().getAxis() ? entry.targetSide().getStepY() * 0.5F : 0,
                                entry.targetSide().getAxis() != entry.sourceSide().getAxis() ? entry.targetSide().getStepZ() * 0.5F : 0
                        ),
                ARGB.colorFromFloat(a, r, g, b));

        // Draw target face
        AABB bb = new AABB(entry.targetSide().getStepX() == 1 ? 0.9 : 0, entry.targetSide().getStepY() == 1 ? 0.9 : 0, entry.targetSide().getStepZ() == 1 ? 0.9 : 0,
                entry.targetSide().getStepX() == -1 ? 0.1 : 1, entry.targetSide().getStepY() == -1 ? 0.1 : 1, entry.targetSide().getStepZ() == -1 ? 0.1 : 1);
        bb = bb
                .move(entry.source())
                .move(entry.targetOffset().getX(), entry.targetOffset().getY(), entry.targetOffset().getZ())
                .inflate(0.05, 0.05, 0.05)
                .inflate(-0.05, -0.05, -0.05);
        // Inspired by SupportBlockRenderer
        Gizmos.cuboid(bb, GizmoStyle.stroke(ARGB.colorFromFloat(a, r, g, b), 2.5f));
    }

}
