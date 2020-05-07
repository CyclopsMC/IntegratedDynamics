package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Dummy ingredients variable.
 * @author rubensworks
 */
public class DummyVariableIngredients extends DummyVariable<ValueObjectTypeIngredients.ValueIngredients> {

    public DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients value) {
        super(ValueTypes.OBJECT_INGREDIENTS, value);
    }

    public DummyVariableIngredients() {
        super(ValueTypes.OBJECT_INGREDIENTS);
    }

}
