package org.cyclops.integrateddynamics.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ItemStackTileEntityVariableRender extends ItemStackTileEntityRenderer {

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.getVariableFacade(itemStackIn);
        variableFacade.renderISTER(itemStackIn, p_239207_2_, matrixStack, buffer, combinedLight, combinedOverlay);
    }

}
