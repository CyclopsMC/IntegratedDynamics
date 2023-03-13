package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;

/**
 * Renderer for cable components.
 * @author rubensworks
 */
public class RenderCable implements BlockEntityRenderer<BlockEntityMultipartTicking> {

    public static final RenderType RENDER_TYPE_LINE = RenderType.create(Reference.MOD_ID + "line",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES, 128, false, false, RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(1)))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .createCompositeState(false));

    private final BlockEntityRendererProvider.Context context;

    public RenderCable(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(BlockEntityMultipartTicking tile, float partialTicks, PoseStack matrixStack,
                       MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        Player player = Minecraft.getInstance().player;
        boolean showOffsets = player.getItemInHand(InteractionHand.MAIN_HAND).is(WrenchHelpers.TAG_WRENCH)
                || player.getItemInHand(InteractionHand.OFF_HAND).is(WrenchHelpers.TAG_WRENCH);

        for (Map.Entry<Direction, IPartType<?, ?>> entry : tile.getPartContainer().getParts().entrySet()) {
            // Draw part overlays
            for (IPartOverlayRenderer renderer : PartOverlayRenderers.REGISTRY.getRenderers(entry.getValue())) {
                renderer.renderPartOverlay(this.context, tile.getPartContainer(), entry.getKey(), entry.getValue(),
                        partialTicks, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay);
            }

            // If the player is holding a wrench, show the offsets of parts
            // Only do this for parts with non-default target side or non-default offset
            if (showOffsets) {
                renderOffsets(tile, entry.getKey(), entry.getValue(), tile.getPartContainer().getPartState(entry.getKey()), matrixStack, renderTypeBuffer);
            }
        }
    }

    public void renderOffsets(BlockEntityMultipartTicking tile, Direction side, IPartType partType, IPartState state,
                              PoseStack matrixStack, MultiBufferSource renderTypeBuffer) {
        Vec3i targetOffset = partType.getTargetOffset(state);
        Optional<Direction> targetSideOptional = Optional.ofNullable(partType.getTargetSideOverride(state));
        if (!targetOffset.equals(Vec3i.ZERO) || targetSideOptional.isPresent()) {
            Direction targetSide = targetSideOptional.orElse(side.getOpposite());

            Random posRand = new Random(tile.getBlockPos().asLong());
            float r = 0.5F + posRand.nextFloat() / 2;
            float g = 0.5F + posRand.nextFloat() / 2;
            float b = 0.5F + posRand.nextFloat() / 2;
            float a = 0.90F;

            targetOffset = targetOffset.offset(-side.getOpposite().getStepX(), -side.getOpposite().getStepY(), -side.getOpposite().getStepZ());

            // Draw line from center to target
            VertexConsumer vb = renderTypeBuffer.getBuffer(RENDER_TYPE_LINE);
            float minX = 0.5F + side.getStepX() * 0.5F;
            float minY = 0.5F + side.getStepY() * 0.5F;
            float minZ = 0.5F + side.getStepZ() * 0.5F;
            float maxX = 0.5F + targetOffset.getX() + (targetSide.getAxis() != side.getAxis() ? targetSide.getStepX() * 0.5F : 0);
            float maxY = 0.5F + targetOffset.getY() + (targetSide.getAxis() != side.getAxis() ? targetSide.getStepY() * 0.5F : 0);
            float maxZ = 0.5F + targetOffset.getZ() + (targetSide.getAxis() != side.getAxis() ? targetSide.getStepZ() * 0.5F : 0);
            vb.vertex(matrixStack.last().pose(), minX, minY, minZ).color(r, g, b, a).endVertex();
            vb.vertex(matrixStack.last().pose(), maxX, maxY, maxZ).color(r, g, b, a).endVertex();

            // Draw target face
            AABB bb = new AABB(targetSide.getStepX() == 1 ? 0.9 : 0, targetSide.getStepY() == 1 ? 0.9 : 0, targetSide.getStepZ() == 1 ? 0.9 : 0,
                    targetSide.getStepX() == -1 ? 0.1 : 1, targetSide.getStepY() == -1 ? 0.1 : 1, targetSide.getStepZ() == -1 ? 0.1 : 1);
            bb = bb
                    .move(targetOffset.getX(), targetOffset.getY(), targetOffset.getZ())
                    .inflate(0.05, 0.05, 0.05)
                    .inflate(-0.05, -0.05, -0.05);
            LevelRenderer.renderLineBox(matrixStack, Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.lines()),
                    bb, r, g, b, a);
        }
    }

}
