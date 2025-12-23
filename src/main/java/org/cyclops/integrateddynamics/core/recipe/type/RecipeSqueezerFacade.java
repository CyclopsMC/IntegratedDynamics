package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.item.ItemFacade;

import java.util.Optional;

public class RecipeSqueezerFacade extends RecipeSqueezer {

    public static IngredientChance OUTPUT = new IngredientChance(Either.left(Pair.of(new ItemStack(RegistryEntries.ITEM_FACADE), 1.0f)));

    public static final RecipeSqueezerFacade INSTANCE = new RecipeSqueezerFacade();

    private RecipeSqueezerFacade() {
        super(Ingredient.of(new ItemStack(RegistryEntries.ITEM_FACADE)), NonNullList.of(OUTPUT, OUTPUT), Optional.empty());
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
        IngredientChance outputItemChance = new IngredientChance(inputItemEither.mapBoth(i -> Pair.of(i, 1.0f), i -> Pair.of(i, 1.0f)));
        return NonNullList.of(outputItemChance, outputItemChance, OUTPUT);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        return ((inv.getItem(0).getItem() instanceof ItemFacade) && (inv.getItem(0).has(RegistryEntries.DATACOMPONENT_FACADE_BLOCK)));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_SQUEEZER_FACADE.get();
    }
}
