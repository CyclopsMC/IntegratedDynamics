package org.cyclops.integrateddynamics.api.evaluate.variable;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;

/**
 * A numerical value type.
 * To allow for a good functioning, this requires all types to have mappings to each other in the {@link IValueCastRegistry}.
 * @author rubensworks
 */
public interface IValueTypeNumber<V extends IValue> extends IValueType<V>, IValueTypeNamed<V> {

    public boolean isZero(V a);
    public boolean isOne(V a);
    public V add(V a, V b);
    public V subtract(V a, V b);
    public V multiply(V a, V b);
    public V divide(V a, V b);
    public V max(V a, V b);
    public V min(V a, V b);
    public ValueTypeString.ValueString fuzzy(V a);
    public boolean greaterThan(V a, V b);
    public boolean lessThan(V a, V b);
    public ValueTypeInteger.ValueInteger round(V a);
    public ValueTypeInteger.ValueInteger ceil(V a);
    public ValueTypeInteger.ValueInteger floor(V a);

}
