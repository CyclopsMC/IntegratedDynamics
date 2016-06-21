package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;

/**
 * A default variable implementation.
 * @author rubensworks
 */
public class Variable<V extends IValue> implements IVariable<V> {

    private final IValueType<V> type;
    private final V value;

    public Variable(IValueType<V> type, V value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public IValueType<V> getType() {
        return type;
    }

    @Override
    public V getValue() throws EvaluationException {
        return value;
    }
}
