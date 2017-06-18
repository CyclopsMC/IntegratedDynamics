package org.cyclops.integrateddynamics.client.render.valuetype;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
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
    public void renderValue(IPartContainer partContainer, double x, double y, double z, float partialTick,
                            int destroyStage, EnumFacing direction, IPartType partType, IValue value,
                            TileEntityRendererDispatcher rendererDispatcher, float alpha) {
        ItemStack itemStackOptional = ((ValueObjectTypeItemStack.ValueItemStack) value).getRawValue();
        if(!itemStackOptional.isEmpty()) {
            // ItemStack
            renderItemStack(itemStackOptional, alpha);

            // Stack size
            GlStateManager.pushMatrix();
            GlStateManager.translate(7F, 8.5F, 0.3F);
            String stackSize = String.valueOf(itemStackOptional.getCount());
            float scale = 1F / ((float) stackSize.length() + 1F);
            GlStateManager.scale(scale, scale, 1F);
            rendererDispatcher.getFontRenderer().drawString(stackSize,
                    0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)));
            GlStateManager.popMatrix();
        }
    }

    public static void renderItemStack(ItemStack itemStack, float alpha) {
        // ItemStack
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, -1F);
        GlStateManager.scale(0.78, 0.78, 0.01);

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(40f, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(95F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.popMatrix();

        // Inspired by: https://github.com/jaquadro/StorageDrawers/blob/1.10/src/com/jaquadro/minecraft/storagedrawers/client/renderer/TileEntityDrawersRenderer.java#L180

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1, -1);

        GlStateManager.pushAttrib();
        GlStateManager.enableRescaleNormal();
        GlStateManager.popAttrib();

        renderItem.renderItemIntoGUI(itemStack, 0, 0);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();

        GlStateManager.disablePolygonOffset();

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }
}
