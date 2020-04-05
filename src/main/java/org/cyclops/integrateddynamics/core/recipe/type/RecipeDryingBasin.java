package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Drying basin recipe
 * @author rubensworks
 */
public class RecipeDryingBasin implements IRecipe<IInventoryFluid> {

    private final ResourceLocation id;
    private final Ingredient inputIngredient;
    private final FluidStack inputFluid;
    private final ItemStack outputItem;
    private final FluidStack outputFluid;
    private final int duration;

    public RecipeDryingBasin(ResourceLocation id, Ingredient inputIngredient, FluidStack inputFluid,
                             ItemStack outputItem, FluidStack outputFluid, int duration) {
        this.id = id;
        this.inputIngredient = inputIngredient;
        this.inputFluid = inputFluid;
        this.outputItem = outputItem;
        this.outputFluid = outputFluid;
        this.duration = duration;
    }

    public Ingredient getInputIngredient() {
        return inputIngredient;
    }

    public FluidStack getInputFluid() {
        return inputFluid;
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public boolean matches(IInventoryFluid inv, World worldIn) {
        return inputIngredient.test(inv.getStackInSlot(0))
                && inputFluid.getFluid() == inv.getFluidHandler().getFluidInTank(0).getFluid()
                && inputFluid.getAmount() <= inv.getFluidHandler().getFluidInTank(0).getAmount();
    }

    @Override
    public ItemStack getCraftingResult(IInventoryFluid inv) {
        return this.outputItem.copy();
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height <= 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return this.outputItem;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_DRYING_BASIN;
    }

    @Override
    public IRecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_DRYING_BASIN;
    }
}
