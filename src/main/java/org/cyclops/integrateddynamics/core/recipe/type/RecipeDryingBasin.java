package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;

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

    public ItemStack getOutputItemFirst() {
        return getOutputItem().get().map(l -> l, ItemStackFromIngredient::getFirstItemStack);
    }

    public Optional<FluidStack> getOutputFluid() {
        return outputFluid;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public boolean matches(IInventoryFluid inv, Level worldIn) {
        return inputIngredient.map(p -> p.test(inv.getItem(0))).orElse(true)
                && inputFluid.map(f -> f.getFluid() == inv.getFluidHandler().getFluidInTank(0).getFluid()).orElse(true)
                && inputFluid.map(f -> f.getAmount() <= inv.getFluidHandler().getFluidInTank(0).getAmount()).orElse(true);
    }

    @Override
    public ItemStack assemble(IInventoryFluid inv, HolderLookup.Provider registryAccess) {
        return this.getOutputItemFirst().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height <= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return this.getOutputItemFirst().copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_DRYING_BASIN.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_DRYING_BASIN.get();
    }
}
