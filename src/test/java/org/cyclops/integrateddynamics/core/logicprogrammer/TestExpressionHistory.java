package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for the undo history of expressions in the logic programmer.
 * @author rubensworks
 */
public class TestExpressionHistory {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    private static Expression sum(int a, int b) {
        return new Expression(new ExpressionNode.Operator(Operators.ARITHMETIC_ADDITION, List.of(
                new ExpressionNode.Const(ValueTypeInteger.ValueInteger.of(a)), new ExpressionNode.Const(ValueTypeInteger.ValueInteger.of(b)))));
    }

    private static ExpressionHistory.Entry entry(Expression expression) {
        return new ExpressionHistory.Entry(expression, null);
    }

    @Test
    public void testUndoRedo() {
        ExpressionHistory history = new ExpressionHistory();
        history.observe(sum(1, 1), null);
        history.observe(sum(1, 2), null);
        history.observe(sum(1, 3), null);
        assertThat(history.canRedo(), is(false));

        assertThat(history.undo(), is(entry(sum(1, 2))));
        history.moved(entry(sum(1, 2)));
        assertThat(history.undo(), is(entry(sum(1, 1))));
        history.moved(entry(sum(1, 1)));
        assertThat(history.canUndo(), is(false));
        assertThat(history.undo(), nullValue());

        assertThat(history.redo(), is(entry(sum(1, 2))));
        history.moved(entry(sum(1, 2)));
        assertThat(history.redo(), is(entry(sum(1, 3))));
        history.moved(entry(sum(1, 3)));
        assertThat(history.redo(), nullValue());
    }

    @Test
    public void testNewChangeDropsRedo() {
        ExpressionHistory history = new ExpressionHistory();
        history.observe(sum(1, 1), null);
        history.observe(sum(1, 2), null);
        history.moved(history.undo());
        assertThat(history.canRedo(), is(true));
        history.observe(sum(5, 5), null);
        assertThat(history.canRedo(), is(false));
        assertThat(history.undo(), is(entry(sum(1, 1))));
    }

    @Test
    public void testEqualExpressionIsNoChange() {
        ExpressionHistory history = new ExpressionHistory();
        history.observe(sum(1, 1), null);
        // Such as the same expression deserialized again after a set action
        history.observe(sum(1, 1), null);
        assertThat(history.canUndo(), is(false));
    }

    @Test
    public void testTextIsChange() {
        ExpressionHistory history = new ExpressionHistory();
        history.observe(sum(1, 1), null);
        // The same expression typed in another way
        history.observe(sum(1, 1), "add(1, 1)");
        history.observe(sum(1, 1), "add(1, 1)");
        assertThat(history.undo(), is(entry(sum(1, 1))));
        history.moved(entry(sum(1, 1)));
        assertThat(history.canUndo(), is(false));
        assertThat(history.redo(), is(new ExpressionHistory.Entry(sum(1, 1), "add(1, 1)")));
    }

    @Test
    public void testLimitAndClear() {
        ExpressionHistory history = new ExpressionHistory();
        for (int i = 0; i <= ExpressionHistory.MAX_SIZE + 10; i++) {
            history.observe(sum(0, i), null);
        }
        int undone = 0;
        ExpressionHistory.Entry entry;
        while ((entry = history.undo()) != null) {
            history.moved(entry);
            undone++;
        }
        assertThat(undone, is(ExpressionHistory.MAX_SIZE));

        history.clear();
        assertThat(history.canUndo(), is(false));
        assertThat(history.canRedo(), is(false));
        // The first expression after clearing is no change
        history.observe(sum(9, 9), null);
        assertThat(history.canUndo(), is(false));
    }

}
