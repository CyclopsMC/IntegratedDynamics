package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredientItemStack;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeIngredientsWrapper;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

/**
 * Test the different variable types.
 * @author rubensworks
 */
public class TestVariables {

    @IntegrationTest
    public void testIngredientsType() {
        DummyVariableIngredients inull = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(null));
        TestHelpers.assertEqual(inull.getValue().getRawValue().orNull(), null, "null value is null");

        IIngredients ingredients1 =
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientItemStack(Ingredient.EMPTY),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Items.BOAT)),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE))),
                        new RecipeIngredientItemStack(Ingredient.EMPTY)
                ));
        DummyVariableIngredients i0 = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients
                .of(ingredients1));
        TestHelpers.assertEqual(i0.getValue().getRawValue().get(), ingredients1, "ingredient value is ingredient");

        TestHelpers.assertEqual(i0.getType().serialize(i0.getValue()), "{\"listminecraft:itemstack\":[[],[\"{id:\\\"minecraft:boat\\\",Count:1,Damage:32767s}\"],[\"{id:\\\"minecraft:stone\\\",Count:1,Damage:32767s}\"],[]]}", "Serialization is correct");
        TestHelpers.assertEqual(i0.getType().deserialize("{\"listminecraft:itemstack\":[[],[\"{id:\\\"minecraft:boat\\\",Count:1,Damage:32767s}\"],[\"{id:\\\"minecraft:stone\\\",Count:1,Damage:32767s}\"],[]]}"), i0.getValue(), "Deserialization is correct");
    }

    @IntegrationTest
    public void testRecipeType() {
        DummyVariableRecipe rnull = new DummyVariableRecipe(ValueObjectTypeRecipe.ValueRecipe.of(null));
        TestHelpers.assertEqual(rnull.getValue().getRawValue().orNull(), null, "null value is null");

        IIngredients ingredients1 =
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientItemStack(Ingredient.EMPTY),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Items.BOAT)),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE))),
                        new RecipeIngredientItemStack(Ingredient.EMPTY)
                ));
        IIngredients ingredients2 =
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientItemStack(Ingredient.EMPTY),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Item.getItemFromBlock(Blocks.PLANKS))),
                        new RecipeIngredientItemStack(Ingredient.EMPTY)
                ));
        ValueObjectTypeRecipe.Recipe rawRecipe = new ValueObjectTypeRecipe.Recipe(
                ValueObjectTypeIngredients.ValueIngredients.of(ingredients1),
                ValueObjectTypeIngredients.ValueIngredients.of(ingredients2)
        );
        DummyVariableRecipe r0 = new DummyVariableRecipe(ValueObjectTypeRecipe.ValueRecipe.of(rawRecipe));
        TestHelpers.assertEqual(r0.getValue().getRawValue().get(), rawRecipe, "recipe value is recipe");

        TestHelpers.assertEqual(r0.getType().serialize(r0.getValue()), "{output:\"{\\\"listminecraft:itemstack\\\":[[],[\\\"{id:\\\\\\\"minecraft:planks\\\\\\\",Count:1,Damage:32767s}\\\"],[]]}\",input:\"{\\\"listminecraft:itemstack\\\":[[],[\\\"{id:\\\\\\\"minecraft:boat\\\\\\\",Count:1,Damage:32767s}\\\"],[\\\"{id:\\\\\\\"minecraft:stone\\\\\\\",Count:1,Damage:32767s}\\\"],[]]}\"}", "Serialization is correct");
        TestHelpers.assertEqual(r0.getType().deserialize("{output:\"{\\\"listminecraft:itemstack\\\":[[],[\\\"{id:\\\\\\\"minecraft:planks\\\\\\\",Count:1,Damage:32767s}\\\"],[]]}\",input:\"{\\\"listminecraft:itemstack\\\":[[],[\\\"{id:\\\\\\\"minecraft:boat\\\\\\\",Count:1,Damage:32767s}\\\"],[\\\"{id:\\\\\\\"minecraft:stone\\\\\\\",Count:1,Damage:32767s}\\\"],[]]}\"}"), r0.getValue(), "Deserialization is correct");
    }

}
