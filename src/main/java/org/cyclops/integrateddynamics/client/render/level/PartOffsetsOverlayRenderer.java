package org.cyclops.integrateddynamics.client.render.level;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.network.PartOffsetsClientNotifier;
import org.cyclops.integrateddynamics.network.packet.PartOffsetsSubscribePacket;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;

/**
 * @author rubensworks
 */
public class PartOffsetsOverlayRenderer {

    public static final RenderType RENDER_TYPE_LINE = RenderType.create(Reference.MOD_ID + "line",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES, 128, false, false, RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(1)))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .createCompositeState(false));

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
    public void onRender(RenderLevelStageEvent event) {
        Player player = Minecraft.getInstance().player;
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            // If the player is holding a wrench, show the offsets of parts
            // Only do this for parts with non-default target side or non-default offset
            if (player.getItemInHand(InteractionHand.MAIN_HAND).is(WrenchHelpers.TAG_WRENCH)
                    || player.getItemInHand(InteractionHand.OFF_HAND).is(WrenchHelpers.TAG_WRENCH)) {
                if (!subscribedToServerChanges) {
                    this.subscribeToServerChanges();
                }

                Vec3 eyePos = event.getCamera().getPosition();
                for (PartOffsetsClientNotifier.Entry entry : this.data) {
                    this.renderOffset(event.getPoseStack(), Minecraft.getInstance().renderBuffers().outlineBufferSource(), entry, eyePos);
                }

            } else if (subscribedToServerChanges) {
                this.data.clear();
                this.unsubscribeToServerChanges();
            }
        }
    }

    private void renderOffset(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, PartOffsetsClientNotifier.Entry entry, Vec3 eyePos) {
        double offsetX = eyePos.x;
        double offsetY = eyePos.y;
        double offsetZ = eyePos.z;

        Random posRand = new Random(entry.source().asLong());
        float r = 0.5F + posRand.nextFloat() / 2;
        float g = 0.5F + posRand.nextFloat() / 2;
        float b = 0.5F + posRand.nextFloat() / 2;
        float a = 0.90F;

        // Draw line from center to target
        VertexConsumer vb = renderTypeBuffer.getBuffer(RENDER_TYPE_LINE);
        float minX = entry.source().getX() - (float) offsetX + 0.5F + entry.sourceSide().getStepX() * 0.5F;
        float minY = entry.source().getY() - (float) offsetY + 0.5F + entry.sourceSide().getStepY() * 0.5F;
        float minZ = entry.source().getZ() - (float) offsetZ + 0.5F + entry.sourceSide().getStepZ() * 0.5F;
        float maxX = entry.source().getX() - (float) offsetX + 0.5F + entry.targetOffset().getX() + (entry.targetSide().getAxis() != entry.sourceSide().getAxis() ? entry.targetSide().getStepX() * 0.5F : 0);
        float maxY = entry.source().getY() - (float) offsetY + 0.5F + entry.targetOffset().getY() + (entry.targetSide().getAxis() != entry.sourceSide().getAxis() ? entry.targetSide().getStepY() * 0.5F : 0);
        float maxZ = entry.source().getZ() - (float) offsetZ + 0.5F + entry.targetOffset().getZ() + (entry.targetSide().getAxis() != entry.sourceSide().getAxis() ? entry.targetSide().getStepZ() * 0.5F : 0);
        vb.addVertex(matrixStack.last().pose(), minX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(matrixStack.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a);

        // Draw target face
        AABB bb = new AABB(entry.targetSide().getStepX() == 1 ? 0.9 : 0, entry.targetSide().getStepY() == 1 ? 0.9 : 0, entry.targetSide().getStepZ() == 1 ? 0.9 : 0,
                entry.targetSide().getStepX() == -1 ? 0.1 : 1, entry.targetSide().getStepY() == -1 ? 0.1 : 1, entry.targetSide().getStepZ() == -1 ? 0.1 : 1);
        bb = bb
                .move(entry.source())
                .move(entry.targetOffset().getX(), entry.targetOffset().getY(), entry.targetOffset().getZ())
                .move(-offsetX, -offsetY, -offsetZ)
                .inflate(0.05, 0.05, 0.05)
                .inflate(-0.05, -0.05, -0.05);
        LevelRenderer.renderLineBox(matrixStack, renderTypeBuffer.getBuffer(RenderType.lines()),
                bb, r, g, b, a);
    }

}
