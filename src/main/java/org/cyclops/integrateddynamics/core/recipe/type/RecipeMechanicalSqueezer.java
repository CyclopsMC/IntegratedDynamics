package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplaySqueezer;

import java.util.List;
import java.util.Optional;

/**
 * Mechanical squeezer recipe
 * @author rubensworks
 */
public class RecipeMechanicalSqueezer extends RecipeSqueezer {

    private final int duration;

    public RecipeMechanicalSqueezer(Ingredient inputIngredient,
                                    NonNullList<IngredientChance> outputItems,
                                    Optional<FluidStackTemplate> outputFluid,
                                    int duration) {
        super(inputIngredient, outputItems, outputFluid);
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_SQUEEZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<CraftingInput>> getType() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RegistryEntries.RECIPEBOOKCATEGORY_MECHANICAL_SQUEEZER.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new RecipeDisplaySqueezer(
                this.getInputIngredient().display(),
                this.getOutputItemsAsSlots(),
                this.getOutputFluid().orElse(FluidStack.EMPTY),
                new SlotDisplay.ItemSlotDisplay(RegistryEntries.BLOCK_MECHANICAL_SQUEEZER.get().asItem()),
                this.getDuration()
        ));
    }
}
