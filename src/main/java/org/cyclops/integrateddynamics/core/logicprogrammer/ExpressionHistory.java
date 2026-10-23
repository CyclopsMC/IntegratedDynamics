package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * The changes to an expression in the logic programmer, to go back and forth through them.
 * Changes are recorded by observing the expression, so they can come from slots, parameters or text alike.
 * Typing the same expression in another way is a change as well, as the text is shown as typed.
 * @author rubensworks
 */
public class ExpressionHistory {

    public static final int MAX_SIZE = 64;

    private final Deque<Entry> undo = new ArrayDeque<>();
    private final Deque<Entry> redo = new ArrayDeque<>();
    @Nullable
    private Entry current = null;

    /**
     * Observe the current expression, which records a change if it or its text differs from the one observed before.
     * @param expression The current expression.
     * @param text The text it was typed as, or null if it is printed.
     */
    public void observe(Expression expression, @Nullable String text) {
        if (current != null && expression == current.expression() && Objects.equals(text, current.text())) {
            return;
        }
        Entry entry = new Entry(expression, text);
        if (current != null && !current.equals(entry)) {
            undo.push(current);
            if (undo.size() > MAX_SIZE) {
                undo.removeLast();
            }
            redo.clear();
        }
        current = entry;
    }

    public boolean canUndo() {
        return current != null && !undo.isEmpty();
    }

    public boolean canRedo() {
        return current != null && !redo.isEmpty();
    }

    /**
     * Go back one change. Call {@link #moved(Entry)} once the returned expression is set.
     * @return The expression to go back to, or null if there is none.
     */
    @Nullable
    public Entry undo() {
        if (!canUndo()) {
            return null;
        }
        redo.push(current);
        return undo.pop();
    }

    /**
     * Go forward one undone change. Call {@link #moved(Entry)} once the returned expression is set.
     * @return The expression to go forward to, or null if there is none.
     */
    @Nullable
    public Entry redo() {
        if (!canRedo()) {
            return null;
        }
        undo.push(current);
        return redo.pop();
    }

    /**
     * Make the given expression the current one without recording a change, after an undo or redo set it.
     * @param entry The current expression and its text.
     */
    public void moved(Entry entry) {
        current = entry;
    }

    /**
     * Forget all changes, such as when another expression is opened.
     */
    public void clear() {
        undo.clear();
        redo.clear();
        current = null;
    }

    /**
     * @param expression An expression.
     * @param text The text it was typed as, or null if it is printed.
     */
    public static record Entry(Expression expression, @Nullable String text) {}

}
