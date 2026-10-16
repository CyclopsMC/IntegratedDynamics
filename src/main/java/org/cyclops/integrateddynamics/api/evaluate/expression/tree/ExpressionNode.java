package org.cyclops.integrateddynamics.api.evaluate.expression.tree;

import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A node in an expression tree, as stored on an expression variable card.
 * Nodes are immutable.
 * @author rubensworks
 */
public sealed interface ExpressionNode permits ExpressionNode.Operator, ExpressionNode.Ref,
        ExpressionNode.Const, ExpressionNode.Param, ExpressionNode.Empty {

    /**
     * A shared empty node instance.
     */
    public static final Empty EMPTY = new Empty();

    /**
     * An operator applied to child nodes.
     * @param operator The operator.
     * @param children The children, one for each operator input.
     */
    public static record Operator(IOperator operator, List<ExpressionNode> children) implements ExpressionNode {
        public Operator {
            Objects.requireNonNull(operator);
            children = List.copyOf(children);
        }

        /**
         * @param operator An operator.
         * @return A node for the given operator with all children empty.
         */
        public static Operator ofEmpty(IOperator operator) {
            ExpressionNode[] children = new ExpressionNode[operator.getRequiredInputLength()];
            Arrays.fill(children, EMPTY);
            return new Operator(operator, List.of(children));
        }

        /**
         * @param index A child index.
         * @param child The new child.
         * @return A copy of this node with the given child replaced.
         */
        public Operator withChild(int index, ExpressionNode child) {
            ExpressionNode[] newChildren = children.toArray(new ExpressionNode[0]);
            newChildren[index] = child;
            return new Operator(operator, List.of(newChildren));
        }
    }

    /**
     * A reference to another variable card in the network, by variable id.
     * @param variableId The variable id.
     */
    public static record Ref(int variableId) implements ExpressionNode {}

    /**
     * An inline constant value.
     * @param value The value.
     */
    public static record Const(IValue value) implements ExpressionNode {
        public Const {
            Objects.requireNonNull(value);
        }
    }

    /**
     * A reference to a parameter of the expression.
     * @param index The parameter index.
     */
    public static record Param(int index) implements ExpressionNode {}

    /**
     * An unfilled slot, only present while editing.
     */
    public static final class Empty implements ExpressionNode {
        private Empty() {}

        @Override
        public String toString() {
            return "Empty";
        }
    }

}
