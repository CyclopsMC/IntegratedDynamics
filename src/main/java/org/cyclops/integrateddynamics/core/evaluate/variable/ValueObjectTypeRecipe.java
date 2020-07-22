package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import lombok.ToString;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeRecipeLPElement;

/**
 * Value type with values that are recipes.
 * @author rubensworks
 */
public class ValueObjectTypeRecipe extends ValueObjectTypeBase<ValueObjectTypeRecipe.ValueRecipe> implements
        IValueTypeNamed<ValueObjectTypeRecipe.ValueRecipe>, IValueTypeNullable<ValueObjectTypeRecipe.ValueRecipe> {

    public ValueObjectTypeRecipe() {
        super("recipe", ValueObjectTypeRecipe.ValueRecipe.class);
    }

    @Override
    public ValueRecipe getDefault() {
        return ValueRecipe.of(null);
    }

    @Override
    public ITextComponent toCompactString(ValueRecipe value) {
        if (value.getRawValue().isPresent()) {
            IRecipeDefinition recipe = value.getRawValue().get();
            ITextComponent sb = new StringTextComponent("");

            sb.appendSibling(ValueObjectTypeIngredients.ingredientsToTextComponent(recipe.getOutput()));
            sb.appendSibling(new StringTextComponent(" <- "));
            boolean first = true;

            for (IngredientComponent<?, ?> component : recipe.getInputComponents()) {
                IIngredientComponentHandler handler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
                for (IPrototypedIngredientAlternatives<?, ?> instances : recipe.getInputs(component)) {
                    IPrototypedIngredient<?, ?> prototypedIngredient = Iterables.getFirst(instances.getAlternatives(), null);
                    IValue v;
                    if (prototypedIngredient == null) {
                        v  = handler.getValueType().getDefault();
                    } else {
                        v = handler.toValue(prototypedIngredient.getPrototype());
                    }
                    if (!first) {
                        sb.appendSibling(new StringTextComponent(", "));
                    } else {
                        first = false;
                    }
                    sb.appendSibling(handler.toCompactString(v));
                }
            }
            return sb;
        }
        return new StringTextComponent("");
    }

    @Override
    public INBT serialize(ValueRecipe value) {
        if(!value.getRawValue().isPresent()) return new CompoundNBT();
        return IRecipeDefinition.serialize(value.getRawValue().get());
    }

    @Override
    public ValueRecipe deserialize(INBT value) {
        if (value.getId() == Constants.NBT.TAG_END || (value.getId() == Constants.NBT.TAG_COMPOUND && ((CompoundNBT) value).isEmpty())) {
            return ValueRecipe.of(null);
        }
        try {
            return ValueRecipe.of(IRecipeDefinition.deserialize((CompoundNBT) value));
        } catch (IllegalArgumentException e) {
            return ValueRecipe.of(null);
        }
    }

    @Override
    public String getName(ValueRecipe a) {
        return toCompactString(a).getString();
    }

    @Override
    public boolean isNull(ValueRecipe a) {
        return !a.getRawValue().isPresent();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeRecipeLPElement();
    }

    @ToString
    public static class ValueRecipe extends ValueOptionalBase<IRecipeDefinition> {

        private ValueRecipe(IRecipeDefinition recipe) {
            super(ValueTypes.OBJECT_RECIPE, recipe);
        }

        public static ValueRecipe of(IRecipeDefinition recipe) {
            return new ValueRecipe(recipe);
        }

        @Override
        protected boolean isEqual(IRecipeDefinition a, IRecipeDefinition b) {
            return a.equals(b);
        }
    }

}
