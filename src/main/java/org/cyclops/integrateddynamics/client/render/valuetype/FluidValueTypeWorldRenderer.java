package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;

/**
 * A value type world renderer for fluids.
 * @author rubensworks
 */
public class FluidValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    @Override
    public void submitValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, SubmitNodeCollector nodeCollector,
                            int combinedLight, int combinedOverlay, float alpha) {
        FluidStack fluidStack = ((ValueObjectTypeFluidStack.ValueFluidStack) value).getRawValue();
        if (!fluidStack.isEmpty()) {
            int brightness = Math.max(combinedLight, fluidStack.getFluid().getFluidType().getLightLevel(fluidStack));
            int l2 = brightness >> 0x10 & 0xFFFF;
            int i3 = brightness & 0xFFFF;

            // Fluid
            matrixStack.pushPose();
            TextureAtlasSprite icon = IModHelpersNeoForge.get().getRenderHelpers().getFluidIcon(fluidStack, Direction.UP);
            Triple<Float, Float, Float> color = IModHelpers.get().getBaseHelpers().intToRGB(Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidStack.getFluid().defaultFluidState()).fluidTintSource().colorAsStack(fluidStack));

            nodeCollector.submitCustomGeometry(matrixStack, RenderTypes.text(icon.atlasLocation()), (pose, vb) -> {
                float min = 0F;
                float max = 12.5F;
                float u1 = icon.getU0();
                float u2 = icon.getU1();
                float v1 = icon.getV0();
                float v2 = icon.getV1();
                vb.addVertex(pose, max, max, 0).setColor(color.getLeft(), color.getMiddle(), color.getRight(), alpha).setUv(u2, v2).setUv2(l2, i3);
                vb.addVertex(pose, max, min, 0).setColor(color.getLeft(), color.getMiddle(), color.getRight(), alpha).setUv(u2, v1).setUv2(l2, i3);
                vb.addVertex(pose, min, min, 0).setColor(color.getLeft(), color.getMiddle(), color.getRight(), alpha).setUv(u1, v1).setUv2(l2, i3);
                vb.addVertex(pose, min, max, 0).setColor(color.getLeft(), color.getMiddle(), color.getRight(), alpha).setUv(u1, v2).setUv2(l2, i3);

                // Stack size
                matrixStack.translate(7F, 8.5F, 0.1F);
                String string = String.valueOf(fluidStack.getAmount());
                float scale = ((float) 5) / (float) context.font().width(string);
                matrixStack.scale(scale, scale, 1F);
                nodeCollector.submitText(matrixStack, 0, 0, Component.literal(string).getVisualOrderText(),
                        false, Font.DisplayMode.NORMAL, combinedLight, IModHelpers.get().getBaseHelpers().RGBAToInt(200, 200, 200, (int) (alpha * 255F)),
                        0, 0);
            });

            matrixStack.popPose();
        }
    }

}
