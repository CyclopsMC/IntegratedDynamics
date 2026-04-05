package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplayDryingBasin;

import java.util.List;
import java.util.Optional;

/**
 * Mechanical drying basin recipe
 * @author rubensworks
 */
public class RecipeMechanicalDryingBasin extends RecipeDryingBasin {

    public RecipeMechanicalDryingBasin(Optional<Ingredient> inputIngredient, Optional<FluidStackTemplate> inputFluid,
                                       Optional<Either<ItemStackTemplate, ItemStackFromIngredient>> outputItem, Optional<FluidStackTemplate> outputFluid, int duration) {
        super(inputIngredient, inputFluid, outputItem, outputFluid, duration);
    }

    @Override
    public RecipeSerializer<? extends Recipe<IInventoryFluid>> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_DRYING_BASIN.get();
    }

    @Override
    public RecipeType<? extends Recipe<IInventoryFluid>> getType() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RegistryEntries.RECIPEBOOKCATEGORY_MECHANICAL_DRYING_BASIN.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new RecipeDisplayDryingBasin(
                this.getInputIngredient().map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE),
                this.getInputFluid().orElse(FluidStack.EMPTY),
                this.getOutputItemFirst().<SlotDisplay>map(stack -> new SlotDisplay.ItemStackSlotDisplay(net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(stack))).orElse(SlotDisplay.Empty.INSTANCE),
                this.getOutputFluid().orElse(FluidStack.EMPTY),
                new SlotDisplay.ItemSlotDisplay(RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.get().asItem()),
                this.getDuration()
        ));
    }
}
