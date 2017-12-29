package org.cyclops.integrateddynamics.api.evaluate.expression;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;

import java.util.List;

/**
 * A basic variable implementation.
 * @author rubensworks
 */
public abstract class VariableAdapter<V extends IValue> implements IVariable<V> {

    private List<IVariable<?>> dependents = Lists.newLinkedList();

    @Override
    public boolean canInvalidate() {
        return true;
    }

    @Override
    public void invalidate() {
        for (IVariable<?> dependent : dependents) {
            if (dependent.canInvalidate()) {
                dependent.invalidate();
            }
        }
        dependents.clear();
    }

    @Override
    public void addDependent(IVariable<?> dependent) {
        dependents.add(dependent);
    }
}
