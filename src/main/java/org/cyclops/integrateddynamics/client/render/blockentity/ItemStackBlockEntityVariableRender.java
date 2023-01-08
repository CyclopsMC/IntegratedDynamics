package org.cyclops.integrateddynamics.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ItemStackBlockEntityVariableRender extends BlockEntityWithoutLevelRenderer {

    public ItemStackBlockEntityVariableRender() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemTransforms.TransformType p_239207_2_, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.getVariableFacade(ValueDeseralizationContext.ofClient(), itemStackIn);
        variableFacade.renderISTER(itemStackIn, p_239207_2_, matrixStack, buffer, combinedLight, combinedOverlay);
    }

}
