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
 * @author rubensworks/lostofthought
 */
public final class ValueParseRegistry implements IValueParseRegistry {

    private static ValueParseRegistry INSTANCE = new ValueParseRegistry();

    private final Map<IValueType, IMapping> mappings = Maps.newHashMap();

    private ValueParseRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static ValueParseRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <T2 extends IValueType<V2>, V2 extends IValue> void register(T2 to, IMapping<T2, V2> mapping) {
        mappings.put(to, mapping);
        Operators.REGISTRY.register(new ParseOperator<>(to, mapping));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T2 extends IValueType<V2>, V2 extends IValue> V2 parse(T2 target, ValueTypeString.ValueString value) throws ValueParseException {
        IMapping mapping = mappings.get(target);
        if(mapping == null) {
            throw new ValueParseException(target);
        }
        return ((IMapping<T2, V2>) mapping).parse(value);
    }

    @Override
    public <T2 extends IValueType<V2>, V2 extends IValue> boolean canParse(T2 target) {
        return mappings.containsKey(target);
    }
}
