package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

import javax.annotation.Nullable;

/**
 * A collection of helpers for variables, values and value types.
 * @author rubensworks
 */
public class ValueHelpers {

    /**
     * Create a new value type array from the given variable array element-wise.
     * If a variable would be null, that corresponding value type would be null as well.
     * @param variables The variables.
     * @return The value types array corresponding element-wise to the variables array.
     */
    public static IValueType[] from(IVariable[] variables) {
        IValueType[] valueTypes = new IValueType[variables.length];
        for(int i = 0; i < valueTypes.length; i++) {
            IVariable variable = variables[i];
            valueTypes[i] = variable == null ? null : variable.getType();
        }
        return valueTypes;
    }

    /**
     * Create a new value type array from the given variableFacades array element-wise.
     * If a variableFacade would be null, that corresponding value type would be null as well.
     * @param variableFacades The variables facades.
     * @return The value types array corresponding element-wise to the variables array.
     */
    public static IValueType[] from(IVariableFacade[] variableFacades) {
        IValueType[] valueTypes = new IValueType[variableFacades.length];
        for(int i = 0; i < valueTypes.length; i++) {
            IVariableFacade variableFacade = variableFacades[i];
            valueTypes[i] = variableFacade == null ? null : variableFacade.getOutputType();
        }
        return valueTypes;
    }

    /**
     * Check if the two given values are equal.
     * If they are both null, they are also considered equal.
     * @param v1 Value one
     * @param v2 Value two
     * @return If they are equal.
     */
    public static boolean areValuesEqual(@Nullable IValue v1, @Nullable IValue v2) {
        return v1 == null && v2 == null || (!(v1 == null || v2 == null) && v1.equals(v2));
    }

    /**
     * Bidirectional checking of correspondence.
     * @param t1 First type.
     * @param t2 Second type.
     * @return If they correspond to each other in some direction.
     */
    public static boolean correspondsTo(IValueType t1, IValueType t2) {
        return t1.correspondsTo(t2) || t2.correspondsTo(t1);
    }

}
