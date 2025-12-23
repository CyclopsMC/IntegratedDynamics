package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.item.ItemFacade;

public class RecipeSqueezerFacade extends RecipeSqueezer {

    public static IngredientChance OUTPUT = new IngredientChance(Either.left(new ItemStack(RegistryEntries.ITEM_FACADE)), 1.0f);

    public RecipeSqueezerFacade(ResourceLocation id, Ingredient inputIngredient) {
        super(id, inputIngredient, NonNullList.of(OUTPUT, OUTPUT), FluidStack.EMPTY);
    }

    @Override
    public NonNullList<IngredientChance> assemble(ItemStack inputItem) {
        return getOutput(inputItem);
    }

    public static NonNullList<IngredientChance> getOutput(ItemStack inputItem) {
        ItemFacade inputFacade = (ItemFacade)inputItem.getItem();
        ItemStack facadeBlockItem = inputFacade.getFacadeBlockItem(inputItem);

        if (facadeBlockItem == null) return NonNullList.of(OUTPUT, OUTPUT);
        Either<ItemStack, ItemStackFromIngredient> inputItemEither = Either.left(facadeBlockItem);
        IngredientChance outputItemChance = new IngredientChance(inputItemEither, 1.0f);
        return NonNullList.of(outputItemChance, outputItemChance, OUTPUT);
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return ((inv.getItem(0).getItem() instanceof ItemFacade) && (inv.getItem(0).hasTag()));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_SQUEEZER_FACADE;
    }
}
