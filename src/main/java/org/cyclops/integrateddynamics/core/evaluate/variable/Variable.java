package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.Data;

/**
 * A default variable implementation.
 * @author rubensworks
 */
@Data
public class Variable<V extends IValue> implements IVariable<V> {

    private final IValueType<V> type;
    private final V value;

}
