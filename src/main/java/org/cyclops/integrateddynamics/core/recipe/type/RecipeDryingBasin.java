package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.display.RecipeDisplayDryingBasin;

import java.util.List;
import java.util.Optional;

/**
 * Drying basin recipe
 * @author rubensworks
 */
public class RecipeDryingBasin implements Recipe<IInventoryFluid> {

    private final Optional<Ingredient> inputIngredient;
    private final Optional<FluidStack> inputFluid;
    private final Optional<Either<ItemStack, ItemStackFromIngredient>> outputItem;
    private final Optional<FluidStack> outputFluid;
    private final int duration;

    public RecipeDryingBasin(Optional<Ingredient> inputIngredient, Optional<FluidStack> inputFluid,
                             Optional<Either<ItemStack, ItemStackFromIngredient>> outputIngredient, Optional<FluidStack> outputFluid, int duration) {
        this.inputIngredient = inputIngredient;
        this.inputFluid = inputFluid;
        this.outputItem = outputIngredient;
        this.outputFluid = outputFluid;
        this.duration = duration;
    }

    public Optional<Ingredient> getInputIngredient() {
        return inputIngredient;
    }

    public Optional<FluidStack> getInputFluid() {
        return inputFluid;
    }

    public Optional<Either<ItemStack, ItemStackFromIngredient>> getOutputItem() {
        return outputItem;
    }

    public Optional<ItemStack> getOutputItemFirst() {
        return getOutputItem().map(either -> either.map(l -> l, ItemStackFromIngredient::getFirstItemStack));
    }

    public Optional<FluidStack> getOutputFluid() {
        return outputFluid;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public boolean matches(IInventoryFluid inv, Level worldIn) {
        return inputIngredient.map(p -> p.test(inv.getItem(0))).orElse(inv.getItem(0).isEmpty())
                && inputFluid.map(f -> f.getFluid() == inv.getFluidHandler().getFluidInTank(0).getFluid()).orElse(inv.getFluidHandler().getFluidInTank(0).isEmpty())
                && inputFluid.map(f -> f.getAmount() <= inv.getFluidHandler().getFluidInTank(0).getAmount()).orElse(inv.getFluidHandler().getFluidInTank(0).isEmpty());
    }

    @Override
    public ItemStack assemble(IInventoryFluid inv, HolderLookup.Provider registryAccess) {
        return this.getOutputItemFirst().get().copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<IInventoryFluid>> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_DRYING_BASIN.get();
    }

    @Override
    public RecipeType<? extends Recipe<IInventoryFluid>> getType() {
        return RegistryEntries.RECIPETYPE_DRYING_BASIN.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RegistryEntries.RECIPEBOOKCATEGORY_DRYING_BASIN.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new RecipeDisplayDryingBasin(
                this.getInputIngredient().map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE),
                this.getInputFluid().orElse(FluidStack.EMPTY),
                this.getOutputItemFirst().<SlotDisplay>map(SlotDisplay.ItemStackSlotDisplay::new).orElse(SlotDisplay.Empty.INSTANCE),
                this.getOutputFluid().orElse(FluidStack.EMPTY),
                new SlotDisplay.ItemSlotDisplay(RegistryEntries.BLOCK_DRYING_BASIN.get().asItem()),
                this.getDuration()
        ));
    }
}
