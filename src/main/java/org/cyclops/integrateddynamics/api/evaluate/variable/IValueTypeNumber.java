package org.cyclops.integrateddynamics.api.evaluate.variable;

/**
 * A numerical value type.
 * To allow for a good functioning, this requires all types to have mappings to each other in the {@link IValueCastRegistry}.
 * @author rubensworks
 */
public interface IValueTypeNumber<V extends IValue> extends IValueType<V> {

    public boolean isZero(V a);
    public boolean isOne(V a);
    public V add(V a, V b);
    public V subtract(V a, V b);
    public V multiply(V a, V b);
    public V divide(V a, V b);
    public V max(V a, V b);
    public V min(V a, V b);

}
