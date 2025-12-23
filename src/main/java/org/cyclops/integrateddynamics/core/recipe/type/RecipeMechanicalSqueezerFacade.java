package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.item.ItemFacade;

public class RecipeMechanicalSqueezerFacade extends RecipeMechanicalSqueezer {

    public RecipeMechanicalSqueezerFacade(ResourceLocation id, Ingredient inputIngredient, int duration) {
        super(id, inputIngredient, NonNullList.of(RecipeSqueezerFacade.OUTPUT, RecipeSqueezerFacade.OUTPUT), FluidStack.EMPTY, duration);
    }

    @Override
    public NonNullList<IngredientChance> assemble(ItemStack inputItem) {
      return RecipeSqueezerFacade.getOutput(inputItem);
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return ((inv.getItem(0).getItem() instanceof ItemFacade) && (inv.getItem(0).hasTag()));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_SQUEEZER_FACADE;
    }
}
