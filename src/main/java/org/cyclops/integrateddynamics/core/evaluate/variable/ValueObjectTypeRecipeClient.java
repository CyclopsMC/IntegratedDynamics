package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
    public void renderISTER(ValueObjectTypeRecipe.ValueRecipe value, ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (IModHelpers.get().getMinecraftClientHelpers().isShifted()) {
            value.getRawValue()
                    .ifPresent((recipe) -> {
                        List<ItemStack> itemStacks = recipe.getOutput().getInstances(IngredientComponent.ITEMSTACK);
                        if (!itemStacks.isEmpty()) {
                            ItemStack actualStack = itemStacks.get(0);
                            Minecraft.getInstance().getItemRenderer().renderStatic(actualStack, transformType, combinedLight, combinedOverlay, matrixStack, buffer, Minecraft.getInstance().level, 0);
                        }
                    });
        }
    }

}
