package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Strings;
import lombok.ToString;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeRecipeLPElement;

/**
 * Value type with values that are recipes.
 * @author rubensworks
 */
public class ValueObjectTypeRecipe extends ValueObjectTypeBase<ValueObjectTypeRecipe.ValueRecipe> implements
        IValueTypeNamed<ValueObjectTypeRecipe.ValueRecipe>, IValueTypeNullable<ValueObjectTypeRecipe.ValueRecipe> {

    public ValueObjectTypeRecipe() {
        super("recipe");
    }

    @Override
    public ValueRecipe getDefault() {
        return ValueRecipe.of(null);
    }

    @Override
    public String toCompactString(ValueRecipe value) {
        if (value.getRawValue().isPresent()) {
            ValueObjectTypeIngredients.ValueIngredients input = value.getRawValue().get().getInput();
            ValueObjectTypeIngredients.ValueIngredients output = value.getRawValue().get().getOutput();
            return ValueTypes.OBJECT_INGREDIENTS.toCompactString(output)
                    + " <- " + ValueTypes.OBJECT_INGREDIENTS.toCompactString(input);
        }
        return "";
    }

    @Override
    public String serialize(ValueRecipe value) {
        if(!value.getRawValue().isPresent()) return "";
        Recipe recipe = value.getRawValue().get();
        String input = ValueTypes.OBJECT_INGREDIENTS.serialize(recipe.getInput());
        String output = ValueTypes.OBJECT_INGREDIENTS.serialize(recipe.getOutput());
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("input", input);
        tag.setString("output", output);
        return tag.toString();
    }

    @Override
    public ValueRecipe deserialize(String value) {
        if(Strings.isNullOrEmpty(value)) return ValueRecipe.of(null);
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
            ValueObjectTypeIngredients.ValueIngredients input = ValueTypes.OBJECT_INGREDIENTS
                    .deserialize(tag.getString("input"));
            ValueObjectTypeIngredients.ValueIngredients output = ValueTypes.OBJECT_INGREDIENTS
                    .deserialize(tag.getString("output"));
            return ValueRecipe.of(new Recipe(input, output));
        } catch (NBTException | RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Something went wrong while deserializing '%s'.", value));
        }
    }

    @Override
    public String getName(ValueRecipe a) {
        return toCompactString(a);
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
    public static class ValueRecipe extends ValueOptionalBase<Recipe> {

        private ValueRecipe(Recipe recipe) {
            super(ValueTypes.OBJECT_RECIPE, recipe);
        }

        public static ValueRecipe of(Recipe recipe) {
            return new ValueRecipe(recipe);
        }

        @Override
        protected boolean isEqual(Recipe a, Recipe b) {
            return a.equals(b);
        }
    }

    public static class Recipe {

        private final ValueObjectTypeIngredients.ValueIngredients input;
        private final ValueObjectTypeIngredients.ValueIngredients output;

        public Recipe(ValueObjectTypeIngredients.ValueIngredients input,
                      ValueObjectTypeIngredients.ValueIngredients output) {
            this.input = input;
            this.output = output;
        }

        public ValueObjectTypeIngredients.ValueIngredients getInput() {
            return input;
        }

        public ValueObjectTypeIngredients.ValueIngredients getOutput() {
            return output;
        }

        @Override
        public String toString() {
            return "input: [" + ValueTypes.OBJECT_INGREDIENTS.toCompactString(getInput())
                    + "]; output: [" + ValueTypes.OBJECT_INGREDIENTS.toCompactString(getOutput()) + "]";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Recipe
                    && this.getInput().equals(((Recipe) obj).getInput())
                    && this.getOutput().equals(((Recipe) obj).getOutput());
        }
    }

}
