package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Base value implementation for nullable values.
 * @author rubensworks
 */
public abstract class ValueOptionalBase<T> extends ValueBase {

    private final Optional<T> value;

    public ValueOptionalBase(IValueType type, @Nullable T value) {
        super(type);
        this.value = Optional.ofNullable(preprocessValue(value));
    }

    @Nullable
    protected T preprocessValue(@Nullable T value) {
        return value;
    }

    /**
     * @return The raw value in an optional holder.
     */
    public Optional<T> getRawValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if((o instanceof ValueOptionalBase) && getType() == ((ValueOptionalBase) o).getType()) {
            if (((ValueOptionalBase) o).getRawValue().isPresent() && getRawValue().isPresent()) {
                return isEqual(((ValueOptionalBase<T>) o).getRawValue().get(), getRawValue().get());
            } else if (!((ValueOptionalBase) o).getRawValue().isPresent() && !getRawValue().isPresent()) {
                return true;
            }
        }
        return false;
    }

    protected abstract boolean isEqual(T a, T b);

    @Override
    public int hashCode() {
        return getType().hashCode() + (getRawValue().isPresent() ? getRawValue().get().hashCode() : 0);
    }

    @Override
    public String toString() {
        return getRawValue().toString();
    }
}
