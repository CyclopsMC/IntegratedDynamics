package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Dummy recipe variable.
 * @author rubensworks
 */
public class DummyVariableRecipe extends DummyVariable<ValueObjectTypeRecipe.ValueRecipe> {

    public DummyVariableRecipe(ValueObjectTypeRecipe.ValueRecipe value) {
        super(ValueTypes.OBJECT_RECIPE, value);
    }

    public DummyVariableRecipe() {
        super(ValueTypes.OBJECT_RECIPE);
    }

}
