package org.cyclops.integrateddynamics.api.advancement.criterion;

import org.cyclops.integrateddynamics.api.item.IVariableFacade;

/**
 * A predicate for variable facades of all types.
 * @author rubensworks
 */
public class VariableFacadePredicate<V extends IVariableFacade> {

    public static final VariableFacadePredicate ANY = new VariableFacadePredicate<>(IVariableFacade.class);

    private final Class<V> variableClass;

    public VariableFacadePredicate(Class<V> variableClass) {
        this.variableClass = variableClass;
    }

    public final boolean test(IVariableFacade variableFacade) {
        return variableClass.isInstance(variableFacade) && testTyped((V) variableFacade);
    }

    protected boolean testTyped(V variableFacade) {
        return true;
    }

}
