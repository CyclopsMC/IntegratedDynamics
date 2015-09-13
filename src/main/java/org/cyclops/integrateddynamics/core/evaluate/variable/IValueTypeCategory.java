package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Abstraction layer above value type to categorize them so that a certain category can be used inside operators among other things.
 * @author rubensworks
 */
public interface IValueTypeCategory<V extends IValue> extends IValueType<V> {

}
