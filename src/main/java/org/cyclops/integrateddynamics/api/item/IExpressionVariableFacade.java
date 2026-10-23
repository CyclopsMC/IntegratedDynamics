package org.cyclops.integrateddynamics.api.item;

import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Variable facade for variables that are defined by an expression tree,
 * which can contain nested operators, references to other variables, inline constants and parameters.
 *
 * Expressions without parameters evaluate to the value of their root node,
 * expressions with parameters evaluate to an operator.
 *
 * Simple expressions (a single operator with only variable references) are stored as {@link IOperatorVariableFacade} instead.
 * @author rubensworks
 */
public interface IExpressionVariableFacade extends IVariableFacade {

    /**
     * @return The expression, or null if the facade is invalid.
     */
    @Nullable
    public Expression getExpression();

    /**
     * @return The output types of the referenced variables, by variable id, as far as they were known when this card
     *         was written. This is only used when editing the card, evaluation always uses the actual variables.
     */
    public default Map<Integer, IValueType<?>> getReferenceTypes() {
        return Map.of();
    }

    /**
     * @return The text the expression was typed as, or null if it was not typed or is printed.
     *         This is only used when editing the card, and only while the text still reads as the expression.
     */
    @Nullable
    public default String getText() {
        return null;
    }

}
