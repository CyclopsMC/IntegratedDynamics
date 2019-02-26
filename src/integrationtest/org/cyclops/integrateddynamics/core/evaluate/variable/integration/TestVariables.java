package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test the different variable types.
 * @author rubensworks
 */
public class TestVariables {

    @IntegrationTest
    public void testIngredientsType() {
        DummyVariableIngredients inull = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(null));
        TestHelpers.assertEqual(inull.getValue().getRawValue().orNull(), null, "null value is null");

        IMixedIngredients ingredients1 =
                MixedIngredients.ofInstances(IngredientComponent.ITEMSTACK, Lists.newArrayList(
                        ItemStack.EMPTY, new ItemStack(Items.BOAT), new ItemStack(Blocks.STONE), ItemStack.EMPTY));
        DummyVariableIngredients i0 = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients
                .of(ingredients1));
        TestHelpers.assertEqual(i0.getValue().getRawValue().get(), ingredients1, "ingredient value is ingredient");

        TestHelpers.assertEqual(i0.getType().serialize(i0.getValue()), "{\"minecraft:itemstack\":[{id:\"minecraft:air\",Count:1b,Damage:0s},{id:\"minecraft:boat\",Count:1b,Damage:0s},{id:\"minecraft:stone\",Count:1b,Damage:0s},{id:\"minecraft:air\",Count:1b,Damage:0s}]}", "Serialization is correct");
        TestHelpers.assertEqual(i0.getType().deserialize("{\"minecraft:itemstack\":[{id:\"minecraft:air\",Count:1b,Damage:0s},{id:\"minecraft:boat\",Count:1b,Damage:0s},{id:\"minecraft:stone\",Count:1b,Damage:0s},{id:\"minecraft:air\",Count:1b,Damage:0s}]}"), i0.getValue(), "Deserialization is correct");
    }

    @IntegrationTest
    public void testRecipeType() {
        DummyVariableRecipe rnull = new DummyVariableRecipe(ValueObjectTypeRecipe.ValueRecipe.of(null));
        TestHelpers.assertEqual(rnull.getValue().getRawValue().orNull(), null, "null value is null");

        List<List<IPrototypedIngredient<ItemStack, Integer>>> ingredientsIn = Lists.newArrayList();
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Items.BOAT), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Blocks.STONE), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));

        Map<IngredientComponent<?, ?>, List<?>> ingredientsOut = Maps.newIdentityHashMap();
        ingredientsOut.put(IngredientComponent.ENERGY, Lists.newArrayList(777));
        ingredientsOut.put(IngredientComponent.FLUIDSTACK, Lists.newArrayList(new FluidStack(FluidRegistry.WATER, 123)));
        ingredientsOut.put(IngredientComponent.ITEMSTACK, Lists.newArrayList(new ItemStack(Items.BOAT), new ItemStack(Item.getItemFromBlock(Blocks.STONE))));
        DummyVariableIngredients iMainOut = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new MixedIngredients(ingredientsOut)));
        IRecipeDefinition rawRecipe = RecipeDefinition.ofIngredients(
                IngredientComponent.ITEMSTACK,
                ingredientsIn,
                iMainOut.getValue().getRawValue().get()
        );
        DummyVariableRecipe r0 = new DummyVariableRecipe(ValueObjectTypeRecipe.ValueRecipe.of(rawRecipe));
        TestHelpers.assertEqual(r0.getValue().getRawValue().get(), rawRecipe, "recipe value is recipe");

        TestHelpers.assertEqual(r0.getType().serialize(r0.getValue()), "{output:{\"minecraft:energy\":[777],\"minecraft:fluidstack\":[{FluidName:\"water\",Amount:123}],\"minecraft:itemstack\":[{id:\"minecraft:boat\",Count:1b,Damage:0s},{id:\"minecraft:stone\",Count:1b,Damage:0s}]},input:{\"minecraft:itemstack\":[{val:[{condition:15,prototype:{id:\"minecraft:air\",Count:1b,Damage:0s}}],type:0b},{val:[{condition:15,prototype:{id:\"minecraft:boat\",Count:1b,Damage:0s}}],type:0b},{val:[{condition:15,prototype:{id:\"minecraft:stone\",Count:1b,Damage:0s}}],type:0b},{val:[{condition:15,prototype:{id:\"minecraft:air\",Count:1b,Damage:0s}}],type:0b}]}}", "Serialization is correct");
        TestHelpers.assertEqual(r0.getType().deserialize("{output:{\"minecraft:energy\":[777],\"minecraft:fluidstack\":[{FluidName:\"water\",Amount:123}],\"minecraft:itemstack\":[{id:\"minecraft:boat\",Count:1b,Damage:0s},{id:\"minecraft:stone\",Count:1b,Damage:0s}]},input:{\"minecraft:itemstack\":[{val:[{condition:15,prototype:{id:\"minecraft:air\",Count:1b,Damage:0s}}],type:0b},{val:[{condition:15,prototype:{id:\"minecraft:boat\",Count:1b,Damage:0s}}],type:0b},{val:[{condition:15,prototype:{id:\"minecraft:stone\",Count:1b,Damage:0s}}],type:0b},{val:[{condition:15,prototype:{id:\"minecraft:air\",Count:1b,Damage:0s}}],type:0b}]}}"), r0.getValue(), "Deserialization is correct");
    }

}
