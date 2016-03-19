package org.cyclops.integrateddynamics.api.evaluate.variable;

import java.util.Collection;

/**
 * Abstraction layer above value type to categorize them so that a certain category can be used inside operators among other things.
 * @author rubensworks
 */
public interface IValueTypeCategory<V extends IValue> extends IValueType<V> {
    /**
     * @return The value types that are part of this category.
     */
    public Collection<IValueType<?>> getElements();
}
