package org.cyclops.integrateddynamics.api.evaluate.expression;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariableInvalidateListener;

import java.util.Set;

/**
 * A basic variable implementation.
 * @author rubensworks
 */
public abstract class VariableAdapter<V extends IValue> implements IVariable<V> {

    private Set<IVariableInvalidateListener> invalidateListeners = Sets.newIdentityHashSet();

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
