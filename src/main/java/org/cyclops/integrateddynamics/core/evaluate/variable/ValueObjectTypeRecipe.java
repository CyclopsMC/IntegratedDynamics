package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    protected ValueObjectTypeRecipeClient constructClient() {
        return new ValueObjectTypeRecipeClient(this);
    }

    @Override
    public ValueRecipe getDefault() {
        return ValueRecipe.of(null);
    }

    @Override
    public MutableComponent toCompactString(ValueRecipe value) {
        if (value.getRawValue().isPresent()) {
            IRecipeDefinition recipe = value.getRawValue().get();
            MutableComponent sb = Component.literal("");

            sb.append(ValueObjectTypeIngredients.ingredientsToTextComponent(recipe.getOutput()));
            sb.append(Component.literal(" <- "));
            boolean first = true;

            for (IngredientComponent<?, ?> component : recipe.getInputComponents()) {
                IIngredientComponentHandler handler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
                int i = 0;
                for (IPrototypedIngredientAlternatives<?, ?> instances : recipe.getInputs(component)) {
                    IPrototypedIngredient<?, ?> prototypedIngredient = Iterables.getFirst(instances.getAlternatives(), null);
                    IValue v;
                    if (prototypedIngredient == null) {
                        v  = handler.getValueType().getDefault();
                    } else {
                        v = handler.toValue(prototypedIngredient.getPrototype());
                    }
                    if (!first) {
                        sb.append(Component.literal(", "));
                    } else {
                        first = false;
                    }
                    sb.append(handler.toCompactString(v));
                    if (recipe.isInputReusable(component, i)) {
                        sb.append("*");
                    }
                    i++;
                }
            }
            return sb;
        }
        return Component.literal("");
    }

    @Override
    public void serialize(ValueOutput valueOutput, ValueRecipe value) {
        value.getRawValue().ifPresent(v -> IRecipeDefinition.serialize(valueOutput.child("v"), v));
    }

    @Override
    public ValueRecipe deserialize(ValueInput valueInput) {
        try {
            return ValueRecipe.of(valueInput.child("v").map(IRecipeDefinition::deserialize).orElse(null));
        } catch (IllegalArgumentException | JsonParseException e) {
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

        @Override
        public String toString() {
            return "ValueRecipe()";
        }
    }

}
