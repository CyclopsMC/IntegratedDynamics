package org.cyclops.integrateddynamics.api.advancement.criterion;

import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;

import java.util.Optional;

/**
 * A predicate for variables of all types.
 * @author rubensworks
 */
public class VariablePredicate<V extends IVariable> {

    public static final VariablePredicate ANY = new VariablePredicate<>(IVariable.class, Optional.empty(), Optional.empty());

    private final Class<V> variableClass;
    private final Optional<IValueType> valueType;
    private final Optional<ValuePredicate> valuePredicate;

    public VariablePredicate(Class<V> variableClass, Optional<IValueType> valueType, Optional<ValuePredicate> valuePredicate) {
        this.variableClass = variableClass;
        this.valueType = valueType;
        this.valuePredicate = valuePredicate;
    }

    public Optional<IValueType> getValueType() {
        return valueType;
    }

    public Optional<ValuePredicate> getValuePredicate() {
        return valuePredicate;
    }

    public final boolean test(IVariable variable) {
        try {
            return variableClass.isInstance(variable)
                    && (this.valueType.isEmpty() || ValueHelpers.correspondsTo(this.valueType.get(), variable.getType()))
                    && valuePredicate.orElse(ValuePredicate.ANY).test(variable.getValue())
                    && testTyped((V) variable);
        } catch (EvaluationException e) {
            return false;
        }
    }

    protected boolean testTyped(V variable) {
        return true;
    }

}
