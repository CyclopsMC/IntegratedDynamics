package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
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
        itemStacks.add(serializeStack(ItemStack.EMPTY));
        itemStacks.add(serializeStack(new ItemStack(Items.OAK_BOAT)));
        itemStacks.add(serializeStack(new ItemStack(Blocks.STONE)));
        itemStacks.add(serializeStack(ItemStack.EMPTY));
        tag.put("minecraft:itemstack", itemStacks);

        TestHelpers.assertEqual(i0.getType().serialize(ValueDeseralizationContext.ofAllEnabled(), i0.getValue()), tag, "Serialization is correct");
        TestHelpers.assertEqual(i0.getType().deserialize(ValueDeseralizationContext.ofAllEnabled(), tag), i0.getValue(), "Deserialization is correct");
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
        ListTag energies = new ListTag();
        energies.add(LongTag.valueOf(777L));
        output.put("minecraft:energy", energies);
        ListTag itemStacks = new ListTag();
        itemStacks.add(serializeStack(new ItemStack(Items.OAK_BOAT)));
        itemStacks.add(serializeStack(new ItemStack(Blocks.STONE)));
        output.put("minecraft:itemstack", itemStacks);
        ListTag fluidStacks = new ListTag();
        fluidStacks.add(serializeFluidStack(new FluidStack(Fluids.WATER, 123)));
        output.put("minecraft:fluidstack", fluidStacks);

        CompoundTag input = new CompoundTag();
        ListTag itemStacksIn = new ListTag();
        itemStacksIn.add(new CompoundTag());
        itemStacksIn.add(new CompoundTag());
        itemStacksIn.add(new CompoundTag());
        itemStacksIn.add(new CompoundTag());

        ListTag val0 = new ListTag();
        val0.add(IPrototypedIngredient.serialize(ingredientsIn.get(0).get(0)));
        val0.getCompound(0).remove("ingredientComponent");
        itemStacksIn.getCompound(0).put("val", val0);
        itemStacksIn.getCompound(0).putByte("type", (byte) 0);

        ListTag val1 = new ListTag();
        val1.add(IPrototypedIngredient.serialize(ingredientsIn.get(1).get(0)));
        val1.getCompound(0).remove("ingredientComponent");
        itemStacksIn.getCompound(1).put("val", val1);
        itemStacksIn.getCompound(1).putByte("type", (byte) 0);

        ListTag val2 = new ListTag();
        val2.add(IPrototypedIngredient.serialize(ingredientsIn.get(2).get(0)));
        val2.getCompound(0).remove("ingredientComponent");
        itemStacksIn.getCompound(2).put("val", val2);
        itemStacksIn.getCompound(2).putByte("type", (byte) 0);

        ListTag val3 = new ListTag();
        val3.add(IPrototypedIngredient.serialize(ingredientsIn.get(3).get(0)));
        val3.getCompound(0).remove("ingredientComponent");
        itemStacksIn.getCompound(3).put("val", val3);
        itemStacksIn.getCompound(3).putByte("type", (byte) 0);

        input.put("minecraft:itemstack", itemStacksIn);

        CompoundTag inputReusable = new CompoundTag();
        List<Byte> reusableBytes = Lists.newArrayList();
        reusableBytes.add((byte) 0);
        reusableBytes.add((byte) 0);
        reusableBytes.add((byte) 0);
        reusableBytes.add((byte) 0);
        ByteArrayTag itemStacksReusable = new ByteArrayTag(reusableBytes);
        inputReusable.put("minecraft:itemstack", itemStacksReusable);

        tag.put("output", output);
        tag.put("input", input);
        tag.put("inputReusable", inputReusable);

        TestHelpers.assertEqual(r0.getType().serialize(ValueDeseralizationContext.ofAllEnabled(), r0.getValue()), tag, "Serialization is correct");
        TestHelpers.assertEqual(r0.getType().deserialize(ValueDeseralizationContext.ofAllEnabled(), tag), r0.getValue(), "Deserialization is correct");
    }

}
