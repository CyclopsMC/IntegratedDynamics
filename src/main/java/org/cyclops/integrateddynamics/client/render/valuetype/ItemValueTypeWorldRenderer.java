package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;

/**
 * A value type world renderer for items.
 * @author rubensworks
 */
public class ItemValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    @Override
    public void submitValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, SubmitNodeCollector nodeCollector,
                            int combinedLight, int combinedOverlay, float alpha) {
        ItemStack itemStackOptional = ((ValueObjectTypeItemStack.ValueItemStack) value).getRawValue();
        if(!itemStackOptional.isEmpty()) {
            // ItemStack
            renderItemStack(matrixStack, nodeCollector, combinedLight, combinedOverlay, 0, itemStackOptional, alpha);

            // Stack size
            matrixStack.pushPose();
            matrixStack.translate(7F, 8.5F, 0.3F);
            String stackSize = String.valueOf(itemStackOptional.getCount());
            float scale = 1F / ((float) stackSize.length() + 1F);
            matrixStack.scale(scale, scale, 1F);
            nodeCollector.submitText(matrixStack, 0, 0, Component.literal(stackSize).getVisualOrderText(),
                    false, Font.DisplayMode.NORMAL, combinedLight, IModHelpers.get().getBaseHelpers().RGBAToInt(200, 200, 200, (int) (alpha * 255F)), 0, 0);
            matrixStack.popPose();
        }
    }

    public static void renderItemStack(PoseStack matrixStack, SubmitNodeCollector nodeCollector, int combinedLight, int combinedOverlay, int outlineColor, ItemStack itemStack, float alpha) {
        // ItemStack
        matrixStack.pushPose();
        matrixStack.translate(6.2, 6.2, 0.1F);
        matrixStack.scale(16F, -16F, 16F);
        matrixStack.scale(0.74F, 0.74F, 0.01F);

        // Derived from ItemRenderer
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, itemStack, ItemDisplayContext.GUI, null, null, 0);
        renderState.submit(matrixStack, nodeCollector, combinedLight, combinedOverlay, outlineColor);

        matrixStack.popPose();
    }
}
