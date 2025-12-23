package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.item.ItemFacade;

import java.util.Optional;

public class RecipeMechanicalSqueezerFacade extends RecipeMechanicalSqueezer {

    public RecipeMechanicalSqueezerFacade(int duration) {
        super(Ingredient.of(new ItemStack(RegistryEntries.ITEM_FACADE)), NonNullList.of(RecipeSqueezerFacade.OUTPUT, RecipeSqueezerFacade.OUTPUT), Optional.empty(), duration);
    }

    @Override
    public NonNullList<IngredientChance> assemble(ItemStack inputItem) {
      return RecipeSqueezerFacade.getOutput(inputItem);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        return ((inv.getItem(0).getItem() instanceof ItemFacade) && (inv.getItem(0).has(RegistryEntries.DATACOMPONENT_FACADE_BLOCK)));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_SQUEEZER_FACADE.get();
    }
}
