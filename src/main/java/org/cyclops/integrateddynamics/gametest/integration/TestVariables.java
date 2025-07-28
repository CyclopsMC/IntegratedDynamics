package org.cyclops.integrateddynamics.gametest.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.*;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.cyclops.integrateddynamics.core.test.TestHelpers.deserialize;
import static org.cyclops.integrateddynamics.core.test.TestHelpers.serialize;

/**
 * Test the different variable types.
 * @author rubensworks
 */
public class TestVariables {

    protected static Tag serializeStack(ItemStack itemStack) {
        return ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, itemStack)
                .getOrThrow(JsonParseException::new);
    }

    protected static Tag serializeFluidStack(FluidStack fluidStack) {
        return FluidStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, fluidStack)
                .getOrThrow(JsonParseException::new);
    }

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

        CompoundTag tag = new CompoundTag();
        ListTag itemStacks = new ListTag();
        CompoundTag itemStack1 = new CompoundTag();
        itemStack1.put("i", serializeStack(ItemStack.EMPTY));
        itemStacks.add(itemStack1);
        CompoundTag itemStack2 = new CompoundTag();
        itemStack2.put("i", serializeStack(new ItemStack(Items.OAK_BOAT)));
        itemStacks.add(itemStack2);
        CompoundTag itemStack3 = new CompoundTag();
        itemStack3.put("i", serializeStack(new ItemStack(Blocks.STONE)));
        itemStacks.add(itemStack3);
        CompoundTag itemStack4 = new CompoundTag();
        itemStack4.put("i", serializeStack(ItemStack.EMPTY));
        itemStacks.add(itemStack4);
        ListTag ingredientsList = new ListTag();
        CompoundTag ingredientsListItemStacks = new CompoundTag();
        ingredientsListItemStacks.putString("component", "minecraft:itemstack");
        ingredientsListItemStacks.put("instances", itemStacks);
        ingredientsList.add(ingredientsListItemStacks);
        tag.put("v", ingredientsList);

        CompoundTag tagRoot = new CompoundTag();
        tagRoot.put("v", tag);

        TestHelpers.assertEqual(serialize(o -> i0.getType().serialize(o, i0.getValue())), tagRoot, "Serialization is correct");
        TestHelpers.assertEqual(deserialize(tagRoot, i0.getType()::deserialize), i0.getValue(), "Deserialization is correct");
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

        CompoundTag tag = new CompoundTag();

        CompoundTag output = new CompoundTag();
        ListTag outputList = new ListTag();
        output.put("v", outputList);
        ListTag energies = new ListTag();
        CompoundTag energy = new CompoundTag();
        energy.putLong("i", 777L);
        energies.add(energy);
        CompoundTag outputListEnergy = new CompoundTag();
        outputListEnergy.putString("component", "minecraft:energy");
        outputListEnergy.put("instances", energies);
        outputList.add(outputListEnergy);
        ListTag fluidStacks = new ListTag();
        CompoundTag fluidStack1 = new CompoundTag();
        fluidStack1.put("i", serializeFluidStack(new FluidStack(Fluids.WATER, 123)));
        fluidStacks.add(fluidStack1);
        CompoundTag outputListFluidStack = new CompoundTag();
        outputListFluidStack.putString("component", "minecraft:fluidstack");
        outputListFluidStack.put("instances", fluidStacks);
        outputList.add(outputListFluidStack);
        ListTag itemStacks = new ListTag();
        CompoundTag itemStack1 = new CompoundTag();
        itemStack1.put("i", serializeStack(new ItemStack(Items.OAK_BOAT)));
        itemStacks.add(itemStack1);
        CompoundTag itemStack2 = new CompoundTag();
        itemStack2.put("i", serializeStack(new ItemStack(Blocks.STONE)));
        itemStacks.add(itemStack2);
        CompoundTag outputListItemStack = new CompoundTag();
        outputListItemStack.putString("component", "minecraft:itemstack");
        outputListItemStack.put("instances", itemStacks);
        outputList.add(outputListItemStack);

        ListTag input = new ListTag();
        ListTag itemStacksIn = new ListTag();
        itemStacksIn.add(new CompoundTag());
        itemStacksIn.add(new CompoundTag());
        itemStacksIn.add(new CompoundTag());
        itemStacksIn.add(new CompoundTag());

        ListTag val0 = new ListTag();
        val0.add(serialize(o -> IPrototypedIngredient.serialize(o, ingredientsIn.get(0).get(0))));
        val0.getCompound(0).get().remove("ingredientComponent");
        CompoundTag val0l = new CompoundTag();
        val0l.put("l", val0);
        itemStacksIn.getCompound(0).get().put("val", val0l);
        itemStacksIn.getCompound(0).get().putByte("type", (byte) 0);

        ListTag val1 = new ListTag();
        val1.add(serialize(o -> IPrototypedIngredient.serialize(o, ingredientsIn.get(1).get(0))));
        val1.getCompound(0).get().remove("ingredientComponent");
        CompoundTag val1l = new CompoundTag();
        val1l.put("l", val1);
        itemStacksIn.getCompound(1).get().put("val", val1l);
        itemStacksIn.getCompound(1).get().putByte("type", (byte) 0);

        ListTag val2 = new ListTag();
        val2.add(serialize(o -> IPrototypedIngredient.serialize(o, ingredientsIn.get(2).get(0))));
        val2.getCompound(0).get().remove("ingredientComponent");
        CompoundTag val2l = new CompoundTag();
        val2l.put("l", val2);
        itemStacksIn.getCompound(2).get().put("val", val2l);
        itemStacksIn.getCompound(2).get().putByte("type", (byte) 0);

        ListTag val3 = new ListTag();
        val3.add(serialize(o -> IPrototypedIngredient.serialize(o, ingredientsIn.get(3).get(0))));
        val3.getCompound(0).get().remove("ingredientComponent");
        CompoundTag val3l = new CompoundTag();
        val3l.put("l", val3);
        itemStacksIn.getCompound(3).get().put("val", val3l);
        itemStacksIn.getCompound(3).get().putByte("type", (byte) 0);

        CompoundTag ingredientsListItemStacks = new CompoundTag();
        ingredientsListItemStacks.putString("component", "minecraft:itemstack");
        ingredientsListItemStacks.put("instances", itemStacksIn);
        input.add(ingredientsListItemStacks);

        int[] reusableBytes = new int[]{0, 0, 0, 0};
        IntArrayTag itemStacksReusable = new IntArrayTag(reusableBytes);
        ingredientsListItemStacks.put("reusable", itemStacksReusable);

        tag.put("output", output);
        tag.put("input", input);

        CompoundTag topTag = new CompoundTag();
        topTag.put("v", tag);

        TestHelpers.assertEqual(serialize(o -> r0.getType().serialize(o, r0.getValue())), topTag, "Serialization is correct");
        TestHelpers.assertEqual(deserialize(topTag, r0.getType()::deserialize), r0.getValue(), "Deserialization is correct");
    }

}
