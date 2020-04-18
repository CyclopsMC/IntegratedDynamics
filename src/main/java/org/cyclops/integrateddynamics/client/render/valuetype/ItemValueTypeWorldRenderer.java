package org.cyclops.integrateddynamics.client.render.valuetype;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
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
            matrixStack.push();
            matrixStack.translate(7F, 8.5F, 0.3F);
            String stackSize = String.valueOf(itemStackOptional.getCount());
            float scale = 1F / ((float) stackSize.length() + 1F);
            matrixStack.scale(scale, scale, 1F);
            rendererDispatcher.getFontRenderer().renderString(stackSize,
                    0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)), false, matrixStack.getLast().getMatrix(), renderTypeBuffer, false, 0, combinedLight);
            matrixStack.pop();
        }
    }

    public static void renderItemStack(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay, ItemStack itemStack, float alpha) {
        // ItemStack
        matrixStack.push();
        matrixStack.translate(0, 0, -1F);
        matrixStack.scale(0.78F, 0.78F, 0.01F);

        ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
        matrixStack.rotate(Vector3f.YP.rotationDegrees(40f));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(95F));

        // Inspired by: https://github.com/jaquadro/StorageDrawers/blob/1.15/src/main/java/com/jaquadro/minecraft/storagedrawers/client/renderer/TileEntityDrawersRenderer.java

        renderItem.renderItem(itemStack, ItemCameraTransforms.TransformType.GUI, combinedLight, combinedOverlay, matrixStack, renderTypeBuffer);

        matrixStack.pop();
    }
}
