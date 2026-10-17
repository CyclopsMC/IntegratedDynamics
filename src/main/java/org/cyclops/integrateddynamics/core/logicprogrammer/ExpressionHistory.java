package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The changes to an expression in the logic programmer, to go back and forth through them.
 * Changes are recorded by observing the expression, so they can come from slots, parameters or text alike.
 * @author rubensworks
 */
public class ExpressionHistory {

    public static final int MAX_SIZE = 64;

    private final Deque<Expression> undo = new ArrayDeque<>();
    private final Deque<Expression> redo = new ArrayDeque<>();
    @Nullable
    private Expression current = null;

    /**
     * Observe the current expression, which records a change if it differs from the one observed before.
     * @param expression The current expression.
     */
    public void observe(Expression expression) {
        if (expression == current) {
            return;
        }
        if (current != null && !current.equals(expression)) {
            undo.push(current);
            if (undo.size() > MAX_SIZE) {
                undo.removeLast();
            }
            redo.clear();
        }
        current = expression;
    }

    public boolean canUndo() {
        return current != null && !undo.isEmpty();
    }

    public boolean canRedo() {
        return current != null && !redo.isEmpty();
    }

    /**
     * Go back one change. Call {@link #moved(Expression)} once the returned expression is set.
     * @return The expression to go back to, or null if there is none.
     */
    @Nullable
    public Expression undo() {
        if (!canUndo()) {
            return null;
        }
        redo.push(current);
        return undo.pop();
    }

    /**
     * Go forward one undone change. Call {@link #moved(Expression)} once the returned expression is set.
     * @return The expression to go forward to, or null if there is none.
     */
    @Nullable
    public Expression redo() {
        if (!canRedo()) {
            return null;
        }
        undo.push(current);
        return redo.pop();
    }

    /**
     * Make the given expression the current one without recording a change, after an undo or redo set it.
     * @param expression The current expression.
     */
    public void moved(Expression expression) {
        current = expression;
    }

    /**
     * Forget all changes, such as when another expression is opened.
     */
    public void clear() {
        undo.clear();
        redo.clear();
        current = null;
    }

}
