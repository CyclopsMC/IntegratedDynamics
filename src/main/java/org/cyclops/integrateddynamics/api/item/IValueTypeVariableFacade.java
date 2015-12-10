package org.cyclops.integrateddynamics.api.item;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;

/**
 * Variable facade for variables determined by part aspects.
 * @author rubensworks
 */
public interface IValueTypeVariableFacade<V extends IValue> extends IVariableFacade {

    /**
     * @return The value type of this variable.
     */
    public IValueType<V> getValueType();

    /**
     * @return The value contained in this variable.
     */
    public V getValue();

    /**
     * @return The variable referenced by this facade.
     */
    public IVariable<V> getVariable();

}
