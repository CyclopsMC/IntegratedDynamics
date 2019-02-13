package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IStringConversionRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.StringConversionOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

import java.util.Map;

/**
 * Registry for casting {@link IValue}.
 * @author rubensworks / LostOfThought
 */
public final class StringConversionRegistry implements IStringConversionRegistry {

    private static StringConversionRegistry INSTANCE = new StringConversionRegistry();

    private final Map<Pair<IValueType, IValueType>, IMapping> mappings = Maps.newHashMap();

    private StringConversionRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static StringConversionRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> void register(T1 from, T2 to, IMapping<T1, T2, V1, V2> mapping) {
        mappings.put(Pair.<IValueType, IValueType>of(from, to), mapping);
        Operators.REGISTRY.register(new StringConversionOperator<>(from, to, mapping));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> V2 parse(T2 target, V1 value) throws StringConversionException {
        IMapping mapping = mappings.get(Pair.<IValueType, IValueType>of(value.getType(), target));
        if(mapping == null) {
            throw new StringConversionException(value.getType(), target);
        }
        return ((IMapping<T1, T2, V1, V2>) mapping).convert(value);
    }

    @Override
    public <T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> boolean canParse(T2 target, V1 value) {
        return mappings.containsKey(Pair.<IValueType, IValueType>of(value.getType(), target));
    }
}
