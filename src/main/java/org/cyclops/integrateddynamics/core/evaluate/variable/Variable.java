package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.Data;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;

/**
 * A default variable implementation.
 * @author rubensworks
 */
@Data
public class Variable<V extends IValue> implements IVariable<V> {

    private final IValueType<V> type;
    private final V value;

}
