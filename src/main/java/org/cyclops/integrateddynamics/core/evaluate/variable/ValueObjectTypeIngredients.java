package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Strings;
import lombok.ToString;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IIngredientsSerializerRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IRecipeComponentHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.RecipeComponentHandlers;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeIngredientsLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import java.util.List;

/**
 * Value type with values that are ingredients.
 * @author rubensworks
 */
public class ValueObjectTypeIngredients extends ValueObjectTypeBase<ValueObjectTypeIngredients.ValueIngredients> implements
        IValueTypeNamed<ValueObjectTypeIngredients.ValueIngredients>, IValueTypeNullable<ValueObjectTypeIngredients.ValueIngredients> {

    public static IIngredientsSerializerRegistry SERIALIZERS = IntegratedDynamics._instance.getRegistryManager()
            .getRegistry(IIngredientsSerializerRegistry.class);

    public ValueObjectTypeIngredients() {
        super("ingredients");
    }

    @Override
    public ValueIngredients getDefault() {
        return ValueIngredients.of(null);
    }

    @Override
    public String toCompactString(ValueIngredients value) {
        if (value.getRawValue().isPresent()) {
            StringBuilder sb = new StringBuilder();

            IIngredients ingredients = value.getRawValue().get();
            for (RecipeComponent<?, ?> component : ingredients.getComponents()) {
                IRecipeComponentHandler handler = RecipeComponentHandlers.REGISTRY.getComponentHandler(component);
                for (List<IValue> values : ingredients.getRaw(component)) {
                    sb.append(handler.toCompactString(values));
                    sb.append(", ");
                }
            }

            String str = sb.toString();
            return str.length() >= 2 ? str.substring(0, str.length() - 2) : "";
        }
        return "";
    }

    @Override
    public String serialize(ValueIngredients value) {
        if(!value.getRawValue().isPresent()) return "";

        return SERIALIZERS.serialize(value.getRawValue().get()).toString();
    }

    @Override
    public ValueIngredients deserialize(String value) {
        if(Strings.isNullOrEmpty(value)) return ValueIngredients.of(null);
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
            return ValueIngredients.of(SERIALIZERS.deserialize(tag));
        } catch (NBTException | EvaluationException e) {
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
    public static class ValueIngredients extends ValueOptionalBase<IIngredients> {

        private ValueIngredients(IIngredients recipe) {
            super(ValueTypes.OBJECT_INGREDIENTS, recipe);
        }

        public static ValueIngredients of(IIngredients recipe) {
            return new ValueIngredients(recipe);
        }

        @Override
        protected boolean isEqual(IIngredients a, IIngredients b) {
            return a.equals(b);
        }
    }

}
