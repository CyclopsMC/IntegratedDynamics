package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.client.render.part.DisplayPartOverlayRenderer;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A value type world renderer for blocks.
 * @author rubensworks
 */
public class RecipeValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    private static final IValueTypeWorldRenderer INGREDIENTS_RENDERER = ValueTypeWorldRenderers.REGISTRY
            .getRenderer(ValueTypes.OBJECT_INGREDIENTS);

    @Override
    public void renderValue(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        Optional<IRecipeDefinition> recipeOptional = ((ValueObjectTypeRecipe.ValueRecipe) value).getRawValue();
        if(recipeOptional.isPresent()) {
            IRecipeDefinition recipe = recipeOptional.get();

            matrixStack.push();
            matrixStack.scale(0.5F, 0.5F, 1F);

            matrixStack.push();
            matrixStack.scale(0.3F, 0.3F, 1F);
            rendererDispatcher.getFontRenderer().renderString(L10NHelpers.localize("gui.integrateddynamics.input_short"), 8, 15, Helpers.RGBToInt(255, 255, 255),
                    false, matrixStack.getLast().getMatrix(), renderTypeBuffer, false, 0, combinedLight);
            rendererDispatcher.getFontRenderer().renderString(L10NHelpers.localize("gui.integrateddynamics.output_short"), 46, 15, Helpers.RGBToInt(255, 255, 255),
                    false, matrixStack.getLast().getMatrix(), renderTypeBuffer, false, 0, combinedLight);
            matrixStack.pop();

            matrixStack.translate(0, 2 * DisplayPartOverlayRenderer.MAX / 3, 0);
            renderInput(rendererDispatcher, partContainer, direction, partType, recipe, partialTicks,
                    matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, alpha);
            matrixStack.translate(DisplayPartOverlayRenderer.MAX, 0, 0);
            INGREDIENTS_RENDERER.renderValue(rendererDispatcher, partContainer, direction, partType,
                    ValueObjectTypeIngredients.ValueIngredients.of(recipe.getOutput()), partialTicks,
                    matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, alpha);

            matrixStack.pop();
        }
    }

    protected void renderInput(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                               Direction direction, IPartType partType, IRecipeDefinition recipe, float partialTicks,
                               MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
                               int combinedLight, int combinedOverlay, float alpha) {
        // Get a list of all values
        int ingredientCount = recipe.getInputComponents().stream().mapToInt((c) -> recipe.getInputs(c).size()).sum();
        List<IValue> values = Lists.newArrayListWithExpectedSize(ingredientCount);

        // For ingredients with multiple possibilities, vary them based on the current tick
        int tick = ((int) Minecraft.getInstance().world.getGameTime()) / 30;
        for (IngredientComponent<?, ?> component : recipe.getInputComponents()) {
            IIngredientMatcher<?, ?> matcher = component.getMatcher();
            IIngredientComponentHandler componentHandler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
            Stream<List<IPrototypedIngredient>> inputs = enhanceRecipeInputs(component, recipe);
            inputs.forEach(element ->
                    values.add(componentHandler.toValue(IngredientsValueTypeWorldRenderer.prepareElementForTick(
                            element, tick, () -> new PrototypedIngredient(component, matcher.getEmptyInstance(), matcher.getAnyMatchCondition())).getPrototype())));
        }

        // Render ingredients in a square matrix
        IngredientsValueTypeWorldRenderer.renderGrid(rendererDispatcher, partContainer, direction, partType, values,
                partialTicks, matrixStack, renderTypeBuffer, combinedLight, combinedOverlay, alpha);
    }

    protected <T, M> Stream<List<IPrototypedIngredient>> enhanceRecipeInputs(IngredientComponent<T, M> ingredientComponent,
                                                                             IRecipeDefinition recipe) {
        Stream<IPrototypedIngredientAlternatives<T, M>> inputs = recipe.getInputs(ingredientComponent).stream();
        if (ingredientComponent == IngredientComponent.ITEMSTACK) {
            IIngredientMatcher<ItemStack, Integer> matcher = (IIngredientMatcher<ItemStack, Integer>) ingredientComponent.getMatcher();
            return ((Stream<IPrototypedIngredientAlternatives<ItemStack, Integer>>) (Stream) inputs).map(input -> input
                    .getAlternatives().stream()
                    .map(prototypedIngredient -> Collections.singletonList(prototypedIngredient))
                    .flatMap(List::stream)
                    .collect(Collectors.toList())
            );
        } else {
            return ((Stream<IPrototypedIngredientAlternatives<?, ?>>) (Stream) inputs)
                    .map(p -> Lists.newArrayList(p.getAlternatives()));
        }
    }
}
