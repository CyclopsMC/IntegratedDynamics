package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.advancement.criterion.ValuePredicate;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;

import java.util.Optional;

/**
 * @author rubensworks
 */
public class VariablePredicateTyped extends VariablePredicate<IVariable<?>> {
    private final IVariableFacadeHandler<?> handler;

    public VariablePredicateTyped(IVariableFacadeHandler<?> handler, Optional<IValueType> valueType, Optional<ValuePredicate> valuePredicate) {
        super((Class) IVariable.class, valueType, valuePredicate);
        this.handler = handler;
    }

    public IVariableFacadeHandler<?> getHandler() {
        return handler;
    }

    @Override
    protected boolean testTyped(IVariable<?> variable) {
        return this.handler.isInstance(variable);
    }
}
