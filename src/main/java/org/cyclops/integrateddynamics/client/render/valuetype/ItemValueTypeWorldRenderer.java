package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.helper.Helpers;
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
    public void renderValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        ItemStack itemStackOptional = ((ValueObjectTypeItemStack.ValueItemStack) value).getRawValue();
        if(!itemStackOptional.isEmpty()) {
            // ItemStack
            renderItemStack(matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, itemStackOptional, alpha);

            // Stack size
            matrixStack.pushPose();
            matrixStack.translate(7F, 8.5F, 0.3F);
            String stackSize = String.valueOf(itemStackOptional.getCount());
            float scale = 1F / ((float) stackSize.length() + 1F);
            matrixStack.scale(scale, scale, 1F);
            context.getFont().drawInBatch(stackSize,
                    0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)), false, matrixStack.last().pose(), renderTypeBuffer, false, 0, combinedLight);
            matrixStack.popPose();
        }
    }

    public static void renderItemStack(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay, ItemStack itemStack, float alpha) {
        // ItemStack
        matrixStack.pushPose();
        matrixStack.translate(6.2, 6.2, 0.1F);
        matrixStack.scale(16F, -16F, 16F);
        matrixStack.scale(0.74F, 0.74F, 0.01F);

        ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();

        // Inspired by: https://github.com/jaquadro/StorageDrawers/blob/1.15/src/main/java/com/jaquadro/minecraft/storagedrawers/client/renderer/BlockEntityDrawersRenderer.java

        BakedModel itemModel = renderItem.getModel(itemStack, null, null, 0);
        if (itemModel.isGui3d()) {
            Lighting.setupFor3DItems();
        } else {
            Lighting.setupForFlatItems();
        }

        renderItem.render(itemStack, ItemTransforms.TransformType.GUI, false, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, itemModel);

        Lighting.setupFor3DItems();

        matrixStack.popPose();
    }
}
