package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.base.Optional;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
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
        Optional<ItemStack> itemStackOptional = ((ValueObjectTypeItemStack.ValueItemStack) value).getRawValue();
        if(itemStackOptional.isPresent()) {
            // ItemStack
            renderItemStack(itemStackOptional.get(), alpha);

            // Stack size
            GlStateManager.pushMatrix();
            GlStateManager.translate(7F, 8.5F, 0.1F);
            GlStateManager.scale(0.5F, 0.5F, 1F);
            rendererDispatcher.getFontRenderer().drawString(String.valueOf(itemStackOptional.get().stackSize),
                    0, 0, Helpers.RGBAToInt(200, 200, 200, (int) (alpha * 255F)));
            GlStateManager.popMatrix();
        }
    }

    public static void renderItemStack(ItemStack itemStack, float alpha) {
        // ItemStack
        GlStateManager.pushMatrix();
        GlStateManager.translate(3F, 3F, 0);
        GlStateManager.scale(0.4, 0.4, 0.01);
        RenderHelpers.renderItem(itemStack, ItemCameraTransforms.TransformType.NONE);
        GlStateManager.popMatrix();
    }
}
