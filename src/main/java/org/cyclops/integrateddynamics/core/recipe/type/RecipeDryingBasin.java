package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Drying basin recipe
 * @author rubensworks
 */
public class RecipeDryingBasin implements Recipe<IInventoryFluid> {

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
    public boolean matches(IInventoryFluid inv, Level worldIn) {
        return inputIngredient.test(inv.getItem(0))
                && inputFluid.getFluid() == inv.getFluidHandler().getFluidInTank(0).getFluid()
                && inputFluid.getAmount() <= inv.getFluidHandler().getFluidInTank(0).getAmount();
    }

    @Override
    public ItemStack assemble(IInventoryFluid inv) {
        return this.outputItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height <= 1;
    }

    @Override
    public ItemStack getResultItem() {
        return this.outputItem;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_DRYING_BASIN;
    }

    @Override
    public RecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_DRYING_BASIN;
    }
}
