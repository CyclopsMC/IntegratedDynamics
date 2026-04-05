package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.capability.partcontainer.PartContainerTileMultipartTicking;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Renderer for cable components.
 * @author rubensworks
 */
public class RenderCable implements BlockEntityRenderer<BlockEntityMultipartTicking, RenderCable.CableRenderState> {

    private final BlockEntityRendererProvider.Context context;

    public RenderCable(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public RenderCable.CableRenderState createRenderState() {
        return new RenderCable.CableRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityMultipartTicking blockEntity, RenderCable.CableRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.partContainer = blockEntity.getPartContainer();
        renderState.parts = blockEntity.getPartContainer().getParts();
        renderState.partialTicks = partialTick;
    }

    @Override
    public void submit(RenderCable.CableRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        for (Map.Entry<Direction, IPartType<?, ?>> entry : renderState.parts.entrySet()) {
            // Draw part overlays
            for (IPartOverlayRenderer renderer : PartOverlayRenderers.REGISTRY.getRenderers(entry.getValue())) {
                renderer.submitPartOverlay(this.context, renderState.partContainer, entry.getKey(), entry.getValue(),
                        renderState.partialTicks, poseStack, submitNodeCollector, renderState.lightCoords, 0);
            }
        }
    }

    public static class CableRenderState extends BlockEntityRenderState {
        public PartContainerTileMultipartTicking partContainer;
        public Map<Direction, IPartType<?, ?>> parts;
        public float partialTicks;
    }
}
