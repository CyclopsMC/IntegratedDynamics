package org.cyclops.integrateddynamics.api.evaluate.variable;

/**
 * A value type that has a localized name, mostly this type instance is an object.
 * @author rubensworks
 */
public interface IValueTypeNamed<V extends IValue> extends IValueType<V> {

    public String getName(V a);

}
