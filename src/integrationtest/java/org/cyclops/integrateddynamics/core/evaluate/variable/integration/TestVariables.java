package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
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
        TestHelpers.assertEqual(inull.getValue().getRawValue().orElse(null), null, "null value is null");

        IMixedIngredients ingredients1 =
                MixedIngredients.ofInstances(IngredientComponent.ITEMSTACK, Lists.newArrayList(
                        ItemStack.EMPTY, new ItemStack(Items.OAK_BOAT), new ItemStack(Blocks.STONE), ItemStack.EMPTY));
        DummyVariableIngredients i0 = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients
                .of(ingredients1));
        TestHelpers.assertEqual(i0.getValue().getRawValue().get(), ingredients1, "ingredient value is ingredient");

        CompoundNBT tag = new CompoundNBT();
        ListNBT itemStacks = new ListNBT();
        itemStacks.add(ItemStack.EMPTY.serializeNBT());
        itemStacks.add(new ItemStack(Items.OAK_BOAT).serializeNBT());
        itemStacks.add(new ItemStack(Blocks.STONE).serializeNBT());
        itemStacks.add(ItemStack.EMPTY.serializeNBT());
        tag.put("minecraft:itemstack", itemStacks);

        TestHelpers.assertEqual(i0.getType().serialize(i0.getValue()), tag, "Serialization is correct");
        TestHelpers.assertEqual(i0.getType().deserialize(tag), i0.getValue(), "Deserialization is correct");
    }

    @IntegrationTest
    public void testRecipeType() {
        DummyVariableRecipe rnull = new DummyVariableRecipe(ValueObjectTypeRecipe.ValueRecipe.of(null));
        TestHelpers.assertEqual(rnull.getValue().getRawValue().orElse(null), null, "null value is null");

        List<List<IPrototypedIngredient<ItemStack, Integer>>> ingredientsIn = Lists.newArrayList();
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Items.OAK_BOAT), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Blocks.STONE), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));

        Map<IngredientComponent<?, ?>, List<?>> ingredientsOut = Maps.newIdentityHashMap();
        ingredientsOut.put(IngredientComponent.ENERGY, Lists.newArrayList(777L));
        ingredientsOut.put(IngredientComponent.FLUIDSTACK, Lists.newArrayList(new FluidStack(Fluids.WATER, 123)));
        ingredientsOut.put(IngredientComponent.ITEMSTACK, Lists.newArrayList(new ItemStack(Items.OAK_BOAT), new ItemStack(Item.byBlock(Blocks.STONE))));
        DummyVariableIngredients iMainOut = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new MixedIngredients(ingredientsOut)));
        IRecipeDefinition rawRecipe = RecipeDefinition.ofIngredients(
                IngredientComponent.ITEMSTACK,
                ingredientsIn,
                iMainOut.getValue().getRawValue().get()
        );
        DummyVariableRecipe r0 = new DummyVariableRecipe(ValueObjectTypeRecipe.ValueRecipe.of(rawRecipe));
        TestHelpers.assertEqual(r0.getValue().getRawValue().get(), rawRecipe, "recipe value is recipe");

        CompoundNBT tag = new CompoundNBT();

        CompoundNBT output = new CompoundNBT();
        ListNBT energies = new ListNBT();
        energies.add(LongNBT.valueOf(777L));
        output.put("minecraft:energy", energies);
        ListNBT itemStacks = new ListNBT();
        itemStacks.add(new ItemStack(Items.OAK_BOAT).serializeNBT());
        itemStacks.add(new ItemStack(Blocks.STONE).serializeNBT());
        output.put("minecraft:itemstack", itemStacks);
        ListNBT fluidStacks = new ListNBT();
        fluidStacks.add(new FluidStack(Fluids.WATER, 123).writeToNBT(new CompoundNBT()));
        output.put("minecraft:fluidstack", fluidStacks);

        CompoundNBT input = new CompoundNBT();
        ListNBT itemStacksIn = new ListNBT();
        itemStacksIn.add(new CompoundNBT());
        itemStacksIn.add(new CompoundNBT());
        itemStacksIn.add(new CompoundNBT());
        itemStacksIn.add(new CompoundNBT());

        ListNBT val0 = new ListNBT();
        val0.add(IPrototypedIngredient.serialize(ingredientsIn.get(0).get(0)));
        val0.getCompound(0).remove("ingredientComponent");
        itemStacksIn.getCompound(0).put("val", val0);
        itemStacksIn.getCompound(0).putByte("type", (byte) 0);

        ListNBT val1 = new ListNBT();
        val1.add(IPrototypedIngredient.serialize(ingredientsIn.get(1).get(0)));
        val1.getCompound(0).remove("ingredientComponent");
        itemStacksIn.getCompound(1).put("val", val1);
        itemStacksIn.getCompound(1).putByte("type", (byte) 0);

        ListNBT val2 = new ListNBT();
        val2.add(IPrototypedIngredient.serialize(ingredientsIn.get(2).get(0)));
        val2.getCompound(0).remove("ingredientComponent");
        itemStacksIn.getCompound(2).put("val", val2);
        itemStacksIn.getCompound(2).putByte("type", (byte) 0);

        ListNBT val3 = new ListNBT();
        val3.add(IPrototypedIngredient.serialize(ingredientsIn.get(3).get(0)));
        val3.getCompound(0).remove("ingredientComponent");
        itemStacksIn.getCompound(3).put("val", val3);
        itemStacksIn.getCompound(3).putByte("type", (byte) 0);

        input.put("minecraft:itemstack", itemStacksIn);

        tag.put("output", output);
        tag.put("input", input);

        TestHelpers.assertEqual(r0.getType().serialize(r0.getValue()), tag, "Serialization is correct");
        TestHelpers.assertEqual(r0.getType().deserialize(tag), r0.getValue(), "Deserialization is correct");
    }

}
