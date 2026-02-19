package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeIngredientsLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

/**
 * Value type with values that are ingredients.
 * @author rubensworks
 */
public class ValueObjectTypeIngredients extends ValueObjectTypeBase<ValueObjectTypeIngredients.ValueIngredients> implements
        IValueTypeNamed<ValueObjectTypeIngredients.ValueIngredients>, IValueTypeNullable<ValueObjectTypeIngredients.ValueIngredients> {

    public ValueObjectTypeIngredients() {
        super("ingredients", ValueObjectTypeIngredients.ValueIngredients.class);
    }

    @Override
    public ValueIngredients getDefault() {
        return ValueIngredients.of(null);
    }

    public static MutableComponent ingredientsToTextComponent(IMixedIngredients ingredients) {
        MutableComponent sb = Component.literal("");

        for (IngredientComponent<?, ?> component : ingredients.getComponents()) {
            IIngredientComponentHandler handler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
            for (Object instance : ingredients.getInstances(component)) {
                if (sb.getSiblings().size() > 0) {
                    sb.append(Component.literal(", "));
                }
                sb.append(handler.toCompactString(handler.toValue(instance)));
            }
        }

        return sb;
    }

    @Override
    public MutableComponent toCompactString(ValueIngredients value) {
        if (value.getRawValue().isPresent()) {
            return ingredientsToTextComponent(value.getRawValue().get());
        }
        return Component.literal("");
    }

    @Override
    public void serialize(ValueOutput valueOutput, ValueIngredients value) {
        value.getRawValue().ifPresent(v -> IMixedIngredients.serialize(valueOutput.child("v"), v));
    }

    @Override
    public ValueIngredients deserialize(ValueInput valueInput) {
        return ValueIngredients.of(valueInput.child("v").map(IMixedIngredients::deserialize).orElse(null));
    }

    @Override
    public String getName(ValueIngredients a) {
        return toCompactString(a).getString();
    }

    @Override
    public boolean isNull(ValueIngredients a) {
        return !a.getRawValue().isPresent();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeIngredientsLPElement();
    }

    public static class ValueIngredients extends ValueOptionalBase<IMixedIngredients> {

        private ValueIngredients(IMixedIngredients recipe) {
            super(ValueTypes.OBJECT_INGREDIENTS, recipe);
        }

        public static ValueIngredients of(IMixedIngredients recipe) {
            return new ValueIngredients(recipe);
        }

        @Override
        protected boolean isEqual(IMixedIngredients a, IMixedIngredients b) {
            return a.equals(b);
        }

        @Override
        public String toString() {
            return "ValueIngredients()";
        }
    }

}
