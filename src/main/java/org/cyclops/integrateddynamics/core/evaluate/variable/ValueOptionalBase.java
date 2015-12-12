package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Optional;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

/**
 * Base value implementation for nullable values.
 * @author rubensworks
 */
public abstract class ValueOptionalBase<T> extends ValueBase {

    private final Optional<T> value;

    public ValueOptionalBase(IValueType type, T value) {
        super(type);
        this.value = Optional.fromNullable(value);
    }

    /**
     * @return The raw value in an optional holder.
     */
    public Optional<T> getRawValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ValueOptionalBase && ((ValueOptionalBase) o).value.equals(this.value);
    }

}
