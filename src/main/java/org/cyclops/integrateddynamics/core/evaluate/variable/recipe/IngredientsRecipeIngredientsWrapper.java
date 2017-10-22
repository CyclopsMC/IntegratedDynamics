package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.capability.recipehandler.FluidHandlerRecipeTarget;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeIngredient;
import org.cyclops.commoncapabilities.api.capability.recipehandler.ItemHandlerRecipeTarget;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * An implementation of {@link IIngredients} by wrapping around {@link RecipeIngredients}.
 * @author rubensworks
 */
public class IngredientsRecipeIngredientsWrapper implements IIngredients {

    private final RecipeIngredients ingredients;

    public IngredientsRecipeIngredientsWrapper(RecipeIngredients ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public int getItemStackIngredients() {
        return ingredients.getIngredients(RecipeComponent.ITEMSTACK).size();
    }

    protected List<ValueObjectTypeItemStack.ValueItemStack> recipeIngredientItemStackToList(IRecipeIngredient<ItemStack, ItemHandlerRecipeTarget> input) {
        return Lists.transform(input.getMatchingInstances(), new Function<ItemStack, ValueObjectTypeItemStack.ValueItemStack>() {
            @Nullable
            @Override
            public ValueObjectTypeItemStack.ValueItemStack apply(ItemStack input) {
                return ValueObjectTypeItemStack.ValueItemStack.of(input);
            }
        });
    }

    @Override
    public List<ValueObjectTypeItemStack.ValueItemStack> getItemStacks(int index) {
        return recipeIngredientItemStackToList(ingredients.getIngredients(RecipeComponent.ITEMSTACK).get(index));
    }

    @Override
    public Predicate<ValueObjectTypeItemStack.ValueItemStack> getItemStackPredicate(int index) {
        return valueItemStack -> ingredients.getIngredients(RecipeComponent.ITEMSTACK)
                .get(index).test(valueItemStack.getRawValue());
    }

    @Override
    public List<List<ValueObjectTypeItemStack.ValueItemStack>> getItemStacksRaw() {
        return Lists.transform(ingredients.getIngredients(RecipeComponent.ITEMSTACK), new Function<IRecipeIngredient<ItemStack, ItemHandlerRecipeTarget>, List<ValueObjectTypeItemStack.ValueItemStack>>() {
            @Nullable
            @Override
            public List<ValueObjectTypeItemStack.ValueItemStack> apply(@Nullable IRecipeIngredient<ItemStack, ItemHandlerRecipeTarget> input) {
                return recipeIngredientItemStackToList(input);
            }
        });
    }

    @Override
    public int getFluidStackIngredients() {
        return ingredients.getIngredients(RecipeComponent.FLUIDSTACK).size();
    }

    protected List<ValueObjectTypeFluidStack.ValueFluidStack> recipeIngredientFluidStackToList(IRecipeIngredient<FluidStack, FluidHandlerRecipeTarget> input) {
        return Lists.transform(input.getMatchingInstances(),
                new Function<FluidStack, ValueObjectTypeFluidStack.ValueFluidStack>() {
                    @Nullable
                    @Override
                    public ValueObjectTypeFluidStack.ValueFluidStack apply(FluidStack input) {
                        return ValueObjectTypeFluidStack.ValueFluidStack.of(input);
                    }
                });
    }

    @Override
    public List<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStacks(int index) {
        return recipeIngredientFluidStackToList(ingredients.getIngredients(RecipeComponent.FLUIDSTACK).get(index));
    }

    @Override
    public Predicate<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStackPredicate(int index) {
        return valueFluidStack -> ingredients.getIngredients(RecipeComponent.FLUIDSTACK)
                .get(index).test(valueFluidStack.getRawValue().orNull());
    }

    @Override
    public List<List<ValueObjectTypeFluidStack.ValueFluidStack>> getFluidStacksRaw() {
        return Lists.transform(ingredients.getIngredients(RecipeComponent.FLUIDSTACK),
                new Function<IRecipeIngredient<FluidStack, FluidHandlerRecipeTarget>, List<ValueObjectTypeFluidStack.ValueFluidStack>>() {
                    @Nullable
                    @Override
                    public List<ValueObjectTypeFluidStack.ValueFluidStack> apply(@Nullable IRecipeIngredient<FluidStack, FluidHandlerRecipeTarget> input) {
                        return recipeIngredientFluidStackToList(input);
                    }
                });
    }

    @Override
    public int getEnergyIngredients() {
        return ingredients.getIngredients(RecipeComponent.ENERGY).size();
    }

    protected List<ValueTypeInteger.ValueInteger> recipeIngredientEnergyToList(IRecipeIngredient<Integer, IEnergyStorage> input) {
        return Lists.transform(input.getMatchingInstances(), new Function<Integer, ValueTypeInteger.ValueInteger>() {
            @Nullable
            @Override
            public ValueTypeInteger.ValueInteger apply(@Nullable Integer input) {
                return ValueTypeInteger.ValueInteger.of(input);
            }
        });
    }

    @Override
    public List<ValueTypeInteger.ValueInteger> getEnergies(int index) {
        return recipeIngredientEnergyToList(ingredients.getIngredients(RecipeComponent.ENERGY).get(index));
    }

    @Override
    public Predicate<ValueTypeInteger.ValueInteger> getEnergiesPredicate(int index) {
        return valueInteger -> ingredients.getIngredients(RecipeComponent.ENERGY).get(index)
                .test(valueInteger.getRawValue());
    }

    @Override
    public List<List<ValueTypeInteger.ValueInteger>> getEnergiesRaw() {
        return Lists.transform(ingredients.getIngredients(RecipeComponent.ENERGY),
                new Function<IRecipeIngredient<Integer, IEnergyStorage>, List<ValueTypeInteger.ValueInteger>>() {
                    @Nullable
                    @Override
                    public List<ValueTypeInteger.ValueInteger> apply(@Nullable IRecipeIngredient<Integer, IEnergyStorage> input) {
                        return recipeIngredientEnergyToList(input);
                    }
                });
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof IIngredients
                && this.getItemStacksRaw().equals(((IIngredients) obj).getItemStacksRaw())
                && this.getFluidStacksRaw().equals(((IIngredients) obj).getFluidStacksRaw())
                && this.getEnergiesRaw().equals(((IIngredients) obj).getEnergiesRaw()));
    }

    @Override
    public String toString() {
        return ingredients.toString();
    }
}
