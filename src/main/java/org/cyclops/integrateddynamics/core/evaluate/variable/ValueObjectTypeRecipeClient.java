package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.IModHelpers;

import java.util.List;

/**
 * @author rubensworks
 */
public class ValueObjectTypeRecipeClient extends ValueTypeBaseClient<ValueObjectTypeRecipe.ValueRecipe> {

    public ValueObjectTypeRecipeClient(ValueTypeBase<ValueObjectTypeRecipe.ValueRecipe> valueType) {
        super(valueType);
    }

    @Override
    public void renderISTER(ValueObjectTypeRecipe.ValueRecipe value, ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int combinedLight, int combinedOverlay) {
        if (IModHelpers.get().getMinecraftClientHelpers().isShifted()) {
            value.getRawValue()
                    .ifPresent((recipe) -> {
                        List<ItemStack> itemStacks = recipe.getOutput().getInstances(IngredientComponent.ITEMSTACK);
                        if (!itemStacks.isEmpty()) {
                            ItemStack actualStack = itemStacks.get(0);
                            ItemStackRenderState renderState = new ItemStackRenderState();
                            matrixStack.pushPose();
                            matrixStack.translate(0.03F, 0F, 0F);
                            matrixStack.translate(0F, 0F, -0.15F);
                            matrixStack.scale(0.8F, 0.8F, 0.01F);
                            Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, actualStack, ItemDisplayContext.GUI, Minecraft.getInstance().level, null, 0);
                            renderState.submit(matrixStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
                            matrixStack.popPose();
                        }
                    });
        }
    }

}
