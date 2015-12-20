package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.*;

import java.util.Collections;
import java.util.Map;

/**
 * Value type category with values that are numbers.
 * @author rubensworks
 */
public class ValueTypeCategoryNumber extends ValueTypeCategoryBase<IValue> {

    private static final IValueTypeNumber[] ELEMENTS = new IValueTypeNumber[]{ValueTypes.INTEGER, ValueTypes.DOUBLE, ValueTypes.LONG};
    private static final Map<IValueTypeNumber, Integer> INVERTED_ELEMENTS = Collections.unmodifiableMap(constructInvertedArray(ELEMENTS));

    public ValueTypeCategoryNumber() {
        super("number", Helpers.RGBToInt(243, 245, 4), EnumChatFormatting.GOLD.toString(),
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

    protected IValueTypeNumber getType(IVariable v) {
        return ((IValueTypeNumber) v.getType());
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

}
