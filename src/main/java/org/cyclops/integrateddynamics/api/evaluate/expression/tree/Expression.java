package org.cyclops.integrateddynamics.api.evaluate.expression.tree;

import java.util.List;
import java.util.Objects;

/**
 * An expression: a tree of nodes with an ordered list of parameters.
 * An expression without parameters evaluates to the value of its root,
 * an expression with parameters evaluates to an operator.
 * @param root The root node.
 * @param parameters The parameters.
 * @author rubensworks
 */
public record Expression(ExpressionNode root, List<ExpressionParameter> parameters) {
    public Expression {
        Objects.requireNonNull(root);
        parameters = List.copyOf(parameters);
    }

    public Expression(ExpressionNode root) {
        this(root, List.of());
    }

    public Expression withRoot(ExpressionNode root) {
        return new Expression(root, parameters);
    }

    public Expression withParameters(List<ExpressionParameter> parameters) {
        return new Expression(root, parameters);
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }
}
