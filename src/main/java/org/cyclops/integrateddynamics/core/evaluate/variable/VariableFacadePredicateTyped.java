package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;

/**
 * @author rubensworks
 */
public class VariableFacadePredicateTyped extends VariableFacadePredicate<IVariableFacade> {
    private final IVariableFacadeHandler<?> handler;

    public VariableFacadePredicateTyped(IVariableFacadeHandler<?> handler) {
        super(IVariableFacade.class);
        this.handler = handler;
    }

    public IVariableFacadeHandler<?> getHandler() {
        return handler;
    }

    @Override
    protected boolean testTyped(IVariableFacade variableFacade) {
        return this.handler.isInstance(variableFacade);
    }
}
