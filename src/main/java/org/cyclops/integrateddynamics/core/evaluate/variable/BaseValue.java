package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.Data;
import org.cyclops.integrateddynamics.core.evaluate.InvalidValueTypeException;

/**
 * Base implementation of a variable.
 * @author rubensworks
 */
@Data
public abstract class BaseValue implements IValue {

    private final IValueType type;

    @SuppressWarnings("unchecked")
    @Override
    public <V extends IValue> V cast(IValueType<V> valueType) throws InvalidValueTypeException {
        if(valueType == getType()) {
            return (V) this;
        } else {
            throw new InvalidValueTypeException(String.format("The variable %s could not be cast to the type %s",
                    this, valueType));
        }
    }

}
