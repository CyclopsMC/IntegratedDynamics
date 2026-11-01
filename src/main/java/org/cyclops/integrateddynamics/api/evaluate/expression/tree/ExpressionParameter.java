package org.cyclops.integrateddynamics.api.evaluate.expression.tree;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import java.util.Objects;

/**
 * A named and typed parameter of an expression.
 * @param name The parameter name.
 * @param type The parameter value type.
 * @author rubensworks
 */
public record ExpressionParameter(String name, IValueType<?> type) {
    public ExpressionParameter {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
    }

    public ExpressionParameter withType(IValueType<?> type) {
        return new ExpressionParameter(name, type);
    }

    public ExpressionParameter withName(String name) {
        return new ExpressionParameter(name, type);
    }
}
