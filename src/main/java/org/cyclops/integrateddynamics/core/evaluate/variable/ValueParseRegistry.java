package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueParseRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.operator.ParseOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

import java.util.Map;

/**
 * Registry for parseing {@link IValue}.
 * @author rubensworks
 */
public final class ValueParseRegistry implements IValueParseRegistry {

    private static ValueParseRegistry INSTANCE = new ValueParseRegistry();

    private final Map<Pair<IValueType, IValueType>, IMapping> mappings = Maps.newHashMap();

    private ValueParseRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static ValueParseRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <T2 extends IValueType<V2>, V2 extends IValue> void register(ValueTypeString from, T2 to, IMapping<T2, V2> mapping) {
        mappings.put(Pair.<IValueType, IValueType>of(from, to), mapping);
        Operators.REGISTRY.register(new ParseOperator<>(from, to, mapping));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T2 extends IValueType<V2>, V2 extends IValue> V2 parse(T2 target, ValueTypeString.ValueString value) throws ValueParseException {
        IMapping mapping = mappings.get(Pair.<IValueType, IValueType>of(value.getType(), target));
        if(mapping == null) {
            throw new ValueParseException(value.getType(), target);
        }
        return ((IMapping<T2, V2>) mapping).parse(value);
    }

    @Override
    public <T2 extends IValueType<V2>, V2 extends IValue> boolean canParse(T2 target, ValueTypeString.ValueString value) {
        return mappings.containsKey(Pair.<IValueType, IValueType>of(value.getType(), target));
    }
}
