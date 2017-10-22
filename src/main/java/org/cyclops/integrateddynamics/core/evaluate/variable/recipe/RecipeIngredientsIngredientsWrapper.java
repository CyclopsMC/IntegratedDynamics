package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import java.util.Set;

/**
 * An implementation of {@link RecipeIngredients} by wrapping around {@link IIngredients}.
 * @author rubensworks
 */
public class RecipeIngredientsIngredientsWrapper extends RecipeIngredients {

    private final IIngredients ingredients;

    public RecipeIngredientsIngredientsWrapper(IIngredients ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public int getIngredientsSize() {
        return ingredients.getItemStackIngredients()
                + ingredients.getFluidStackIngredients()
                + ingredients.getEnergyIngredients();
    }

    @Override
    public Set<RecipeComponent> getComponents() {
        Set<RecipeComponent> components = Sets.newHashSet();
        if (ingredients.getItemStackIngredients() > 0) {
            components.add(RecipeComponent.ITEMSTACK);
        }
        if (ingredients.getFluidStackIngredients() > 0) {
            components.add(RecipeComponent.FLUIDSTACK);
        }
        if (ingredients.getEnergyIngredients() > 0) {
            components.add(RecipeComponent.ENERGY);
        }
        return components;
    }

    @Override
    public <T, R> List<IRecipeIngredient<T, R>> getIngredients(RecipeComponent<T, R> component) {
        if (component == RecipeComponent.ITEMSTACK) {
            return Lists.transform(ingredients.getItemStacksRaw(), new Function<List<ValueObjectTypeItemStack.ValueItemStack>,
                    IRecipeIngredient<T, R>>() {
                @Nullable
                @Override
                public IRecipeIngredient<T, R> apply(@Nullable List<ValueObjectTypeItemStack.ValueItemStack> input) {
                    return (IRecipeIngredient<T, R>) new RecipeIngredientValue<ItemStack, ItemHandlerRecipeTarget, ValueObjectTypeItemStack.ValueItemStack>(input, RecipeComponent.ITEMSTACK) {

                        @Override
                        protected ValueObjectTypeItemStack.ValueItemStack elementToValue(ItemStack element) {
                            return ValueObjectTypeItemStack.ValueItemStack.of(element);
                        }

                        @Override
                        protected ItemStack valueToElement(ValueObjectTypeItemStack.ValueItemStack value) {
                            return value.getRawValue();
                        }
                    };
                }
            });
        } else if (component == RecipeComponent.FLUIDSTACK) {
            return Lists.transform(ingredients.getFluidStacksRaw(), new Function<List<ValueObjectTypeFluidStack.ValueFluidStack>,
                    IRecipeIngredient<T, R>>() {
                @Nullable
                @Override
                public IRecipeIngredient<T, R> apply(@Nullable List<ValueObjectTypeFluidStack.ValueFluidStack> input) {
                    return (IRecipeIngredient<T, R>) new RecipeIngredientValue<FluidStack, FluidHandlerRecipeTarget, ValueObjectTypeFluidStack.ValueFluidStack>(input, RecipeComponent.FLUIDSTACK) {

                        @Override
                        protected ValueObjectTypeFluidStack.ValueFluidStack elementToValue(FluidStack element) {
                            return ValueObjectTypeFluidStack.ValueFluidStack.of(element);
                        }

                        @Override
                        protected FluidStack valueToElement(ValueObjectTypeFluidStack.ValueFluidStack value) {
                            return value.getRawValue().orNull();
                        }
                    };
                }
            });
        } else if (component == RecipeComponent.ENERGY) {
            return Lists.transform(ingredients.getEnergiesRaw(), new Function<List<ValueTypeInteger.ValueInteger>,
                    IRecipeIngredient<T, R>>() {
                @Nullable
                @Override
                public IRecipeIngredient<T, R> apply(@Nullable List<ValueTypeInteger.ValueInteger> input) {
                    return (IRecipeIngredient<T, R>) new RecipeIngredientValue<Integer, IEnergyStorage, ValueTypeInteger.ValueInteger>(input, RecipeComponent.ENERGY) {

                        @Override
                        protected ValueTypeInteger.ValueInteger elementToValue(Integer element) {
                            return ValueTypeInteger.ValueInteger.of(element);
                        }

                        @Override
                        protected Integer valueToElement(ValueTypeInteger.ValueInteger value) {
                            return value.getRawValue();
                        }
                    };
                }
            });
        }
        return super.getIngredients(component);
    }

    public IIngredients getIngredients() {
        return ingredients;
    }

    // toString and equals should not be necessary, as this wrapper is only for internal delegation usage,
    // not for storing in IValues.

    public static abstract class RecipeIngredientValue<T, R, V> implements IRecipeIngredient<T, R> {

        private final List<V> values;
        private final RecipeComponent<T, R> recipeComponent;

        public RecipeIngredientValue(List<V> values, RecipeComponent<T, R> recipeComponent) {
            this.values = values;
            this.recipeComponent = recipeComponent;
        }

        @Override
        public RecipeComponent<T, R> getComponent() {
            return this.recipeComponent;
        }

        @Override
        public List<T> getMatchingInstances() {
            return Lists.transform(this.values, new Function<V, T>() {
                @Nullable
                @Override
                public T apply(@Nullable V input) {
                    return valueToElement(input);
                }
            });
        }

        @Override
        public boolean test(T t) {
            return this.values.contains(elementToValue(t));
        }

        protected abstract V elementToValue(T element);
        protected abstract T valueToElement(V value);
    }
}
