package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
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
    public void renderValue(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
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
            rendererDispatcher.getFont().drawInBatch(stackSize,
                    0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)), false, matrixStack.last().pose(), renderTypeBuffer, false, 0, combinedLight);
            matrixStack.popPose();
        }
    }

    public static void renderItemStack(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay, ItemStack itemStack, float alpha) {
        // ItemStack
        matrixStack.pushPose();
        matrixStack.translate(6.2, 6.2, 0.1F);
        matrixStack.scale(16F, -16F, 16F);
        matrixStack.scale(0.74F, 0.74F, 0.01F);

        ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();

        // Inspired by: https://github.com/jaquadro/StorageDrawers/blob/1.15/src/main/java/com/jaquadro/minecraft/storagedrawers/client/renderer/TileEntityDrawersRenderer.java

        IBakedModel itemModel = renderItem.getModel(itemStack, null, null);
        if (itemModel.isGui3d()) {
            RenderHelper.setupFor3DItems();
        } else {
            RenderHelper.setupForFlatItems();
        }

        renderItem.render(itemStack, ItemCameraTransforms.TransformType.GUI, false, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, itemModel);

        RenderHelper.turnOff();

        matrixStack.popPose();
    }
}
