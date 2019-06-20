package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueCastRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

/**
 * Value type category with values that are numbers.
 * @author rubensworks
 */
public class ValueTypeCategoryNumber extends ValueTypeCategoryBase<IValue> implements IValueTypeNamed<IValue> {

    private static final IValueTypeNumber[] ELEMENTS = new IValueTypeNumber[]{ValueTypes.INTEGER, ValueTypes.DOUBLE, ValueTypes.LONG};
    private static final Map<IValueTypeNumber, Integer> INVERTED_ELEMENTS = Collections.unmodifiableMap(constructInvertedArray(ELEMENTS));

    public ValueTypeCategoryNumber() {
        super("number", Helpers.RGBToInt(243, 245, 4), TextFormatting.GOLD.toString(),
                Sets.<IValueType<?>>newHashSet(ELEMENTS));
    }

    private static Map<IValueTypeNumber, Integer> constructInvertedArray(IValueTypeNumber[] elements) {
        Map<IValueTypeNumber, Integer> map = Maps.newHashMap();
        for(int i = 0; i < elements.length; i++) {
            map.put(elements[i], i);
        }
        return map;
    }

    public IValueTypeNumber getLowestType(IValueTypeNumber... types) {
        IValueTypeNumber first = types[0];
        for(int i = 1; i < types.length; i++) {
            if(types[i] != first) {
                int maxIndex = -1;
                for(int j = 0; j < types.length; j++) {
                    IValueTypeNumber v = types[j];
                    if(v != null) {
                        maxIndex = Math.max(maxIndex, INVERTED_ELEMENTS.get(v));
                    }
                }
                return ELEMENTS[maxIndex];
            }
        }
        return first;
    }

    protected IValue castValue(IValueTypeNumber type, IValue value) throws IValueCastRegistry.ValueCastException {
        if(value.getType() == type) {
            return value;
        } else {
            return ValueCastMappings.REGISTRY.cast(type, value);
        }
    }

    protected IValueTypeNumber getType(IVariable v) throws EvaluationException {
        IValueType valueType = v.getType();
        // Special case if the type is ANY.
        if (!(valueType instanceof IValueTypeNumber)) {
            // Don't evaluate the var if not needed,
            // as this may be expensive.
            valueType = v.getValue().getType();
        }
        return ((IValueTypeNumber) valueType);
    }

    public IValue add(IVariable a, IVariable b) throws EvaluationException {
        IValueTypeNumber type = getLowestType(getType(a), getType(b));
        IValue av = castValue(type, a.getValue());
        if (type.isZero(av)) { // If a is neutral element for addition
            return castValue(type, b.getValue());
        } else {
            IValue bv = castValue(type, b.getValue());
            if (type.isZero(bv)) { // If b is neutral element for addition
                return av;
            } else {
                return type.add(av, bv);
            }
        }
    }

    public IValue subtract(IVariable a, IVariable b) throws EvaluationException {
        IValueTypeNumber type = getLowestType(getType(a), getType(b));
        IValue bv = castValue(type, b.getValue());
        if (type.isZero(bv)) { // If b is neutral element for subtraction
            return castValue(type, a.getValue());
        } else {
            IValue av = castValue(type, a.getValue());
            return type.subtract(av, bv);
        }
    }

    public IValue multiply(IVariable a, IVariable b) throws EvaluationException {
        IValueTypeNumber type = getLowestType(getType(a), getType(b));
        IValue av = castValue(type, a.getValue());
        if (type.isZero(av)) { // If a is absorbtion element for multiplication
            return av;
        } else if (type.isOne(av)) { // If a is neutral element for multiplication
            return castValue(type, b.getValue());
        } else {
            IValue bv = castValue(type, b.getValue());
            if (type.isOne(bv)) { // If b is neutral element for multiplication
                return av;
            } else {
                return type.multiply(av, bv);
            }
        }
    }

    public IValue divide(IVariable a, IVariable b) throws EvaluationException {
        IValueTypeNumber type = getLowestType(getType(a), getType(b));
        IValue bv = castValue(type, b.getValue());
        if (type.isZero(bv)) { // You can not divide by zero
            throw new EvaluationException("Division by zero");
        } else if (type.isOne(bv)) { // If b is neutral element for division
            return a.getValue();
        } else {
            IValue av = castValue(type, a.getValue());
            return type.divide(av, bv);
        }
    }

    public IValue max(IVariable a, IVariable b) throws EvaluationException {
        IValueTypeNumber type = getLowestType(getType(a), getType(b));
        return type.max(
                castValue(type, a.getValue()),
                castValue(type, b.getValue())
        );
    }

    public IValue min(IVariable a, IVariable b) throws EvaluationException {
        IValueTypeNumber type = getLowestType(getType(a), getType(b));
        return type.min(
                castValue(type, a.getValue()),
                castValue(type, b.getValue())
        );
    }

    public boolean greaterThan(IVariable a, IVariable b) throws EvaluationException {
        IValueTypeNumber type = getLowestType(getType(a), getType(b));
        return type.greaterThan(
                castValue(type, a.getValue()),
                castValue(type, b.getValue())
        );
    }

    public boolean lessThan(IVariable a, IVariable b) throws EvaluationException {
        IValueTypeNumber type = getLowestType(getType(a), getType(b));
        return type.lessThan(
                castValue(type, a.getValue()),
                castValue(type, b.getValue())
        );
    }

    public IValue abs(IVariable a) throws EvaluationException {
        IValueTypeNumber type = getType(a);
        return type.abs(a.getValue());
    }

    public IValue randRange(IVariable min, IVariable max) throws EvaluationException {
        IValueTypeNumber maxType = getType(max);
        if(lessThan(min, max) && min.getValue() != max.getValue()) {
            Random r = new Random();
            if(ValueHelpers.correspondsTo(maxType, ValueTypes.INTEGER)) {
                int minValue = min.getValue().cast(ValueTypes.INTEGER).getRawValue();
                int maxValue = max.getValue().cast(ValueTypes.INTEGER).getRawValue();
                int originalMax = maxValue;
                if(minValue < 0) {
                    maxValue += Math.abs(minValue);
                }
                return ValueTypeInteger.ValueInteger.of((r.nextInt(maxValue) + minValue) % originalMax);
            } else if (ValueHelpers.correspondsTo(maxType, ValueTypes.DOUBLE)){
                double minValue = min.getValue().cast(ValueTypes.DOUBLE).getRawValue();
                double maxValue = max.getValue().cast(ValueTypes.DOUBLE).getRawValue();
                double originalMax = maxValue;
                if(minValue < 0) {
                    maxValue += Math.abs(minValue);
                }
                return ValueTypeDouble.ValueDouble.of((r.nextDouble() * maxValue + minValue) % originalMax);
            } else if (ValueHelpers.correspondsTo(maxType, ValueTypes.LONG)) {
                long minValue = min.getValue().cast(ValueTypes.LONG).getRawValue();
                long maxValue = max.getValue().cast(ValueTypes.LONG).getRawValue();
                long originalMax = maxValue;
                if(minValue < 0) {
                    maxValue += Math.abs(minValue);
                    return ValueTypeLong.ValueLong.of(((r.nextLong() % maxValue) + minValue) % originalMax);
                } else {
                    return ValueTypeLong.ValueLong.of(Math.abs((r.nextLong() % maxValue) + minValue) % originalMax);
                }
            }
        }
        if(!lessThan(min, max)) {
            throw new EvaluationException("Minimum must be less than Maximum!");
        }
        if(min.getValue() == max.getValue()) {
            throw new EvaluationException("Min cannot equal max!");
        }
        throw new EvaluationException("Unexpected error condition in randRange");
    }

    public ValueTypeInteger.ValueInteger round(IVariable a) throws EvaluationException {
        IValueTypeNumber type = getType(a);
        return type.round(castValue(type, a.getValue()));
    }

    public ValueTypeInteger.ValueInteger ceil(IVariable a) throws EvaluationException {
        IValueTypeNumber type = getType(a);
        return type.ceil(castValue(type, a.getValue()));
    }

    public ValueTypeInteger.ValueInteger floor(IVariable a) throws EvaluationException {
        IValueTypeNumber type = getType(a);
        return type.floor(castValue(type, a.getValue()));
    }

    @Override
    public String getName(IValue a) {
        return ((IValueTypeNamed) a.getType()).getName(a);
    }
}
