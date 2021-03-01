package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.evaluate.operator.CurriedOperator;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import javax.annotation.Nullable;

/**
 * A collection of helpers for variables, values and value types.
 * @author rubensworks
 */
public class ValueHelpers {

    /**
     * Create a new value type array from the given variable array element-wise.
     * If a variable would be null, that corresponding value type would be null as well.
     * @param variables The variables.
     * @return The value types array corresponding element-wise to the variables array.
     */
    public static IValueType[] from(IVariable... variables) {
        IValueType[] valueTypes = new IValueType[variables.length];
        for(int i = 0; i < valueTypes.length; i++) {
            IVariable variable = variables[i];
            valueTypes[i] = variable == null ? null : variable.getType();
        }
        return valueTypes;
    }

    /**
     * Create a new value type array from the given variableFacades array element-wise.
     * If a variableFacade would be null, that corresponding value type would be null as well.
     * @param variableFacades The variables facades.
     * @return The value types array corresponding element-wise to the variables array.
     */
    public static IValueType<?>[] from(IVariableFacade... variableFacades) {
        IValueType<?>[] valueTypes = new IValueType[variableFacades.length];
        for(int i = 0; i < valueTypes.length; i++) {
            IVariableFacade variableFacade = variableFacades[i];
            valueTypes[i] = variableFacade == null ? null : variableFacade.getOutputType();
        }
        return valueTypes;
    }

    /**
     * Create a new unlocalized name array from the given variableFacades array element-wise.
     * @param valueTypes The value types.
     * @return The unlocalized names array corresponding element-wise to the value types array.
     */
    public static ITextComponent[] from(IValueType<?>... valueTypes) {
        ITextComponent[] names = new ITextComponent[valueTypes.length];
        for(int i = 0; i < valueTypes.length; i++) {
            IValueType<?> valueType = valueTypes[i];
            names[i] = new TranslationTextComponent(valueType.getTranslationKey());
        }
        return names;
    }

    /**
     * Check if the two given values are equal.
     * If they are both null, they are also considered equal.
     * @param v1 Value one
     * @param v2 Value two
     * @return If they are equal.
     */
    public static boolean areValuesEqual(@Nullable IValue v1, @Nullable IValue v2) {
        return v1 == null && v2 == null || (!(v1 == null || v2 == null) && v1.equals(v2));
    }

    /**
     * Bidirectional checking of correspondence.
     * @param t1 First type.
     * @param t2 Second type.
     * @return If they correspond to each other in some direction.
     */
    public static boolean correspondsTo(IValueType t1, IValueType t2) {
        return t1.correspondsTo(t2) || t2.correspondsTo(t1);
    }

    /**
     * Evaluate an operator for the given values.
     * @param operator The operator.
     * @param values The values.
     * @return The resulting value.
     * @throws EvaluationException If something went wrong during operator evaluation.
     */
    public static IValue evaluateOperator(IOperator operator, IValue... values) throws EvaluationException {
        IVariable[] variables = new IVariable[values.length];
        for (int i = 0; i < variables.length; i++) {
            IValue value = values[i];
            variables[i] = new Variable<>(value.getType(), value);
        }
        return ValueHelpers.evaluateOperator(operator, variables);
    }

    /**
     * Evaluate an operator for the given variables.
     * @param operator The operator.
     * @param variables The variables.
     * @return The resulting value.
     * @throws EvaluationException If something went wrong during operator evaluation.
     */
    public static IValue evaluateOperator(IOperator operator, IVariable... variables) throws EvaluationException {
        int requiredLength = operator.getRequiredInputLength();
        if (requiredLength == variables.length) {
            return operator.evaluate(variables);
        } else {
            if (variables.length > requiredLength) { // We have MORE variables as input than the operator accepts
                IVariable[] acceptableVariables = ArrayUtils.subarray(variables, 0, requiredLength);
                IVariable[] remainingVariables = ArrayUtils.subarray(variables, requiredLength, variables.length);

                // Pass all required variables to the operator, and forward all remaining ones to the resulting operator
                IValue result = evaluateOperator(operator, acceptableVariables);

                // Error if the result is NOT an operator
                if (result.getType() != ValueTypes.OPERATOR) {
                    throw new EvaluationException(new TranslationTextComponent(L10NValues.OPERATOR_ERROR_CURRYINGOVERFLOW,
                            new TranslationTextComponent(operator.getTranslationKey()),
                            requiredLength,
                            variables.length,
                            new TranslationTextComponent(result.getType().getTranslationKey())));
                }

                // Pass all remaining variables to the resulting operator
                IOperator nextOperator = ((ValueTypeOperator.ValueOperator) result).getRawValue();
                return evaluateOperator(nextOperator, remainingVariables);

            } else { // Else, the given variables only partially take up the required input
                return ValueTypeOperator.ValueOperator.of(new CurriedOperator(operator, variables));
            }
        }
    }

    /**
     * Serialize the given value to a raw tag without its value type.
     * @param value The value.
     * @return The NBT tag.
     */
    public static INBT serializeRaw(IValue value) {
        return value.getType().serialize(value);
    }

    /**
     * Serialize the given value to NBT.
     * @param value The value.
     * @return The NBT tag.
     */
    public static CompoundNBT serialize(IValue value) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("valueType", value.getType().getUniqueName().toString());
        tag.put("value", serializeRaw(value));
        return tag;
    }

    /**
     * Deserialize the given NBT tag to a value.
     * @param tag The NBT tag containing a value.
     * @return The value.
     */
    public static IValue deserialize(CompoundNBT tag) {
        IValueType valueType = ValueTypes.REGISTRY.getValueType(new ResourceLocation(tag.getString("valueType")));
        if (valueType == null) {
            return null;
        }
        return deserializeRaw(valueType, tag.get("value"));
    }

    /**
     * Deserialize the given value string to a value.
     * @param <T> The type of value.
     * @param valueType The value type to deserialize for.
     * @param valueString The value tag.
     * @return The value.
     */
    public static <T extends IValue> T deserializeRaw(IValueType<T> valueType, INBT valueString) {
        return valueType.deserialize(valueString);
    }

    /**
     * Get the string representation of the given value.
     * This is useful for cases when the value needs to be edited in a GUI.
     *
     * This corresponds to {@link #parseString(IValueType, String)}.
     *
     * @param value A value.
     * @param <T> The value type.
     * @return A string representation of the given value.
     */
    public static <T extends IValue> String toString(T value) {
        return value.getType().toString(value);
    }

    /**
     * Parse the given string representation of a value.
     *
     * This corresponds to {@link #toString(IValue)}.
     *
     * @param valueType The value type to parse by.
     * @param value A string representation of a value.
     * @param <T> The value type.
     * @return A value.
     * @throws EvaluationException If parsing failed.
     */
    public static <T extends IValue> T parseString(IValueType<T> valueType, String value) throws EvaluationException {
        return valueType.parseString(value);
    }

    /**
     * Check if the given result (from the given operator) is a boolean.
     * @param predicate A predicate, used for error logging.
     * @param result A result from the given predicate
     * @throws EvaluationException If the value was not a boolean.
     */
    public static void validatePredicateOutput(IOperator predicate, IValue result) throws EvaluationException {
        if (!(result instanceof ValueTypeBoolean.ValueBoolean)) {
            IFormattableTextComponent error = new TranslationTextComponent(
                    L10NValues.OPERATOR_ERROR_WRONGPREDICATE,
                    predicate.getLocalizedNameFull(),
                    new TranslationTextComponent(result.getType().getTranslationKey()),
                    new TranslationTextComponent(ValueTypes.BOOLEAN.getTranslationKey()));
            throw new EvaluationException(error);
        }
    }

    /**
     * Get the human readable value of the given value in a safe way.
     * @param variable A nullable variable.
     * @return A pair of a string and color.
     */
    public static Pair<IFormattableTextComponent, Integer> getSafeReadableValue(@Nullable IVariable variable) {
        IFormattableTextComponent readValue = new StringTextComponent("");
        int readValueColor = 0;
        if (!NetworkHelpers.shouldWork()) {
            readValue = new StringTextComponent("SAFE-MODE");
        } else if(variable != null) {
            try {
                IValue value = variable.getValue();
                readValue = value.getType().toCompactString(value);
                readValueColor = value.getType().getDisplayColor();
            } catch (EvaluationException | NullPointerException | PartStateException e) {
                readValue = new StringTextComponent("ERROR");
                readValueColor = Helpers.RGBToInt(255, 0, 0);
            }
        }
        return Pair.of(readValue, readValueColor);
    }

}
