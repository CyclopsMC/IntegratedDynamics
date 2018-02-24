package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Strings;
import lombok.ToString;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
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
        super("ingredients");
    }

    @Override
    public ValueIngredients getDefault() {
        return ValueIngredients.of(null);
    }

    public static String ingredientsToString(IMixedIngredients ingredients) {
        StringBuilder sb = new StringBuilder();

        for (IngredientComponent<?, ?> component : ingredients.getComponents()) {
            IIngredientComponentHandler handler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
            for (Object instance : ingredients.getInstances(component)) {
                sb.append(handler.toCompactString(handler.toValue(instance)));
                sb.append(", ");
            }
        }

        String str = sb.toString();
        return str.length() >= 2 ? str.substring(0, str.length() - 2) : "";
    }

    @Override
    public String toCompactString(ValueIngredients value) {
        if (value.getRawValue().isPresent()) {
            return ingredientsToString(value.getRawValue().get());
        }
        return "";
    }

    @Override
    public String serialize(ValueIngredients value) {
        if(!value.getRawValue().isPresent()) return "";

        return IMixedIngredients.serialize(value.getRawValue().get()).toString();
    }

    @Override
    public ValueIngredients deserialize(String value) {
        if(Strings.isNullOrEmpty(value)) return ValueIngredients.of(null);
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
            return ValueIngredients.of(IMixedIngredients.deserialize(tag));
        } catch (NBTException | IllegalArgumentException e) {
            return ValueIngredients.of(null);
        }
    }

    @Override
    public String getName(ValueIngredients a) {
        return toCompactString(a);
    }

    @Override
    public boolean isNull(ValueIngredients a) {
        return !a.getRawValue().isPresent();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeIngredientsLPElement();
    }

    @ToString
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
    }

}
