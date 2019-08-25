package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.operator.ParseOperator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of variable types.
 * @author rubensworks/LostOfThought
 */
public class ValueParseOperators {

    private static double numberParser(String s){
        double ret = 0.0;
        Pattern p = Pattern.compile("\\A(?<sign>[+-]?)(?<base>0x|#|0)?\\z", Pattern.CASE_INSENSITIVE);
        return ret;
    }

    public static void load() {}
    public static IOperator PARSE_BOOLEAN = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.BOOLEAN, v -> {
            ValueTypeString.ValueString value = v.getValue(0);
            try {
                Pattern p = Pattern.compile("\\AF(alse)?|[+-]?(0x|#)?0+\\z", Pattern.CASE_INSENSITIVE);
                if( value.getRawValue().isEmpty()
                    || p.matcher(value.getRawValue().trim()).matches()
                    || (Long.decode(value.getRawValue()) == 0))
                {
                    return ValueTypeBoolean.ValueBoolean.of(false);
                } else {
                    return ValueTypeBoolean.ValueBoolean.of(true);
                }
            } catch (NumberFormatException e) {
                return ValueTypeBoolean.ValueBoolean.of(true);
            }
        }));

    public static IOperator PARSE_DOUBLE = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.DOUBLE, v -> {
            ValueTypeString.ValueString value = v.getValue(0);
            // TODO: Floating point Hex/Octal
            try {
                return ValueTypeDouble.ValueDouble.of(Double.parseDouble(value.getRawValue()));
            } catch (NumberFormatException e) {
                try {
                    // \u221E = infinity symbol
                    Pattern p = Pattern.compile("\\A([+-]?)(Inf(inity)?|\u221E)\\z", Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(value.getRawValue().trim());
                    if(m.matches()){
                        if(m.group(1).equals("-")){
                            return ValueTypeDouble.ValueDouble.of(Double.NEGATIVE_INFINITY);
                        }
                        return ValueTypeDouble.ValueDouble.of(Double.POSITIVE_INFINITY);
                    }
                    // Try as a long
                    return ValueTypeDouble.ValueDouble.of((double) Long.decode(value.getRawValue()));
                } catch (NumberFormatException e2) {
                    return ValueTypeDouble.ValueDouble.of(0.0);
                }
            }
        }));

    public static IOperator PARSE_INTEGER = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.INTEGER, v -> {
            ValueTypeString.ValueString value = v.getValue(0);
            try{
                return ValueTypeInteger.ValueInteger.of(Integer.decode(value.getRawValue()));
            } catch (NumberFormatException e) {
                return ValueTypeInteger.ValueInteger.of(0);
            }
        }));

    public static IOperator PARSE_LONG = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.LONG, v -> {
            ValueTypeString.ValueString value = v.getValue(0);
            try {
                return ValueTypeLong.ValueLong.of(Long.decode(value.getRawValue()));
            } catch (NumberFormatException e) {
                return ValueTypeLong.ValueLong.of(0L);
            }
        }));

    public static IOperator PARSE_NBT = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.NBT, v -> {
            ValueTypeString.ValueString value = v.getValue(0);
            try {
                return new ValueTypeNbt().deserialize(value.getRawValue());
            } catch (IllegalArgumentException e) {
                return ValueTypeNbt.ValueNbt.of(null);
            }
        }));

    public static IOperator PARSE_STRING = Operators.REGISTRY.register(new ParseOperator<>(ValueTypes.STRING, value -> value.getValue(0)));
}
