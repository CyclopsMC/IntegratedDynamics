package org.cyclops.integrateddynamics.api.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;

/**
 * Proxy for a list.
 * @param <T> The list element type value type.
 * @param <V> The list element type.
 */
public interface IValueTypeListProxy<T extends IValueType<V>, V extends IValue> extends Iterable<V> {

    /**
     * @return The list length
     * @throws EvaluationException If something went wrong wile getting an element.
     */
    public int getLength() throws EvaluationException;

    /**
     * Get the element at the given index.
     * @param index The index.
     * @return The element at the given index.
     * @throws EvaluationException If something went wrong wile getting an element.
     */
    public V get(int index) throws EvaluationException;

    /**
     * @return The list element value type.
     */
    public T getValueType();

    /**
     * @return The proxy type name that must exist in the {@link IValueTypeListProxyFactoryTypeRegistry}
     * so that this can be correctly (de)serialized.
     */
    public String getName();

    /**
     * @return A short string representation used in guis to show the value.
     */
    public String toCompactString();

    /**
     * @return If this is an infinite list.
     */
    public boolean isInfinite();

}
