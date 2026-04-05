package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplaySqueezer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Squeezer recipe
 * @author rubensworks
 */
public class RecipeSqueezer implements Recipe<CraftingInput> {

    private final Ingredient inputIngredient;
    private final NonNullList<IngredientChance> outputItems;
    private final Optional<FluidStackTemplate> outputFluid;

    private PlacementInfo placementInfo;

    public RecipeSqueezer(Ingredient inputIngredient,
                          NonNullList<IngredientChance> outputItems,
                          Optional<FluidStackTemplate> outputFluid) {
        this.inputIngredient = inputIngredient;
        this.outputItems = outputItems;
        this.outputFluid = outputFluid;
    }

    public Ingredient getInputIngredient() {
        return inputIngredient;
    }

    public NonNullList<IngredientChance> getOutputItems() {
        return outputItems;
    }

    public List<Pair<? extends SlotDisplay, Float>> getOutputItemsAsSlots() {
        return this.getOutputItems().stream().map(i -> i.ingredient.map(
                left -> Pair.of(new SlotDisplay.ItemSlotDisplay(left.getLeft().item()), left.getRight()),
                right -> Pair.of(right.getLeft().getIngredient().display(), right.getRight())
        )).toList();
    }

    public Optional<FluidStackTemplate> getOutputFluidTemplate() {
        return outputFluid;
    }

    public Optional<FluidStack> getOutputFluid() {
        return outputFluid.map(FluidStackTemplate::create);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        return inputIngredient.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        // Should not be called, but let's provide a good fallback
        if (this.outputItems.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.outputItems.get(0).getIngredientFirst().copy();
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    public NonNullList<IngredientChance> assemble(ItemStack inputItem) {
        return getOutputItems();
    }

    @Override
    public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_SQUEEZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<CraftingInput>> getType() {
        return RegistryEntries.RECIPETYPE_SQUEEZER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.inputIngredient);
        }
        return this.placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RegistryEntries.RECIPEBOOKCATEGORY_SQUEEZER.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new RecipeDisplaySqueezer(
                this.getInputIngredient().display(),
                this.getOutputItemsAsSlots(),
                this.getOutputFluid().orElse(FluidStack.EMPTY),
                new SlotDisplay.ItemSlotDisplay(RegistryEntries.BLOCK_SQUEEZER.get().asItem()),
                0
        ));
    }

    public static class IngredientChance {
        private final Either<Pair<ItemStackTemplate, Float>, Pair<ItemStackFromIngredient, Float>> ingredient;

        public IngredientChance(Either<Pair<ItemStackTemplate, Float>, Pair<ItemStackFromIngredient, Float>> ingredient) {
            this.ingredient = Objects.requireNonNull(ingredient);
        }

        public Either<ItemStackTemplate, ItemStackFromIngredient> getIngredient() {
            return ingredient.mapBoth(Pair::getLeft, Pair::getLeft);
        }

        public Either<Pair<ItemStackTemplate, Float>, Pair<ItemStackFromIngredient, Float>> getIngredientChance() {
            return ingredient;
        }

        public ItemStack getIngredientFirst() {
            return getIngredient().map(ItemStackTemplate::create, ItemStackFromIngredient::getFirstItemStack);
        }

        public float getChance() {
            return ingredient.map(Pair::getRight, Pair::getRight);
        }

    }

}
