package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariableInvalidateListener;

/**
 * A dummy boolean variable.
 * @author rubensworks
 */
public class DummyVariable<V extends IValue> implements IVariable<V> {

    private final IValueType<V> type;
    private V value;
    private boolean fetched = false;

    public DummyVariable(IValueType<V> type, V value) {
        this.type = type;
        this.value = value;
    }

    public DummyVariable(IValueType<V> type) {
        this.type = type;
        this.value = type.getDefault();
    }

    @Override
    public IValueType<V> getType() {
        return type;
    }

    @Override
    public V getValue() {
        this.fetched = true;
        return value;
    }

    @Override
    public void invalidate() {

    }

    @Override
    public void addInvalidationListener(IVariableInvalidateListener invalidateListener) {

    }

    public void setValue(V value) {
        this.value = value;
    }

    public boolean isFetched() {
        return this.fetched;
    }
}
