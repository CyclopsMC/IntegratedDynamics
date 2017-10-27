package org.cyclops.integrateddynamics.api.evaluate.variable;

/**
 * A value type that has a unique name, mostly this type instance is an object.
 * @author rubensworks
 */
public interface IValueTypeUniquelyNamed<V extends IValue> extends IValueType<V> {

    public String getUniqueName(V a);

}
