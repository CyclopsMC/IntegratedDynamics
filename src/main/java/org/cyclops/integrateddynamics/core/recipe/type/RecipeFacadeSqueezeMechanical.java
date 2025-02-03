package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.item.ItemFacade;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

public class RecipeFacadeSqueezeMechanical extends RecipeMechanicalSqueezer {
    private IngredientChance facadeItemChance = new IngredientChance(Either.<ItemStack, ItemStackFromIngredient>left(new ItemStack(RegistryEntries.ITEM_FACADE)), 1.0f);

    public RecipeFacadeSqueezeMechanical(ResourceLocation id, Ingredient inputIngredient, NonNullList<IngredientChance> outputItems, FluidStack outputFluid, int duration) {
        super(id, inputIngredient, outputItems, outputFluid, duration);
    }

    public NonNullList<IngredientChance> assemble(ItemStack inputItem) {

        if (!(inputItem.getItem() instanceof ItemFacade)) {
            Either<ItemStack, ItemStackFromIngredient> inputItemEither = Either.left(inputItem);
            IngredientChance inputItemChance = new IngredientChance(inputItemEither, 1.0f);
            return NonNullList.of(inputItemChance, inputItemChance);	// if a non-facade gets passed to this recipe somehow, just return it
        }

        ItemFacade inputFacade = (ItemFacade)inputItem.getItem();
        ItemStack facadeBlockItem = inputFacade.getFacadeBlockItem(inputItem);

        if (facadeBlockItem == null) return NonNullList.of(facadeItemChance, facadeItemChance);
        Either<ItemStack, ItemStackFromIngredient> inputItemEither = Either.left(facadeBlockItem);
        IngredientChance outputItemChance = new IngredientChance(inputItemEither, 1.0f);
        return NonNullList.of(outputItemChance, outputItemChance, facadeItemChance);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_FACADE_SQUEEZE_MECHANICAL;
    }
}
