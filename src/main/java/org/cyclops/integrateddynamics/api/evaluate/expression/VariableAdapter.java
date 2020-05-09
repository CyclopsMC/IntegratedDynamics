package org.cyclops.integrateddynamics.api.evaluate.expression;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariableInvalidateListener;

import java.util.List;

/**
 * A basic variable implementation.
 * @author rubensworks
 */
public abstract class VariableAdapter<V extends IValue> implements IVariable<V> {

    private List<IVariableInvalidateListener> invalidateListeners = Lists.newLinkedList();

    @Override
    public void invalidate() {
        for (IVariableInvalidateListener invalidateListener : Lists.newArrayList(invalidateListeners)) {
            invalidateListener.invalidate();
        }
        invalidateListeners.clear();
    }

    @Override
    public void addInvalidationListener(IVariableInvalidateListener invalidateListener) {
        invalidateListeners.add(invalidateListener);
    }
}
