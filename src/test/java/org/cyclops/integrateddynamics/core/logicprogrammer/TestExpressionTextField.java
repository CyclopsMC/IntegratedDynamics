package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the text of expressions in the logic programmer.
 * @author rubensworks
 */
public class TestExpressionTextField {

    private static int[] follow(String from, String to) {
        int[] values = new int[from.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = i + 1;
        }
        return ExpressionTextField.followChanges(from, to, values, 0);
    }

    @Test
    public void testUnchanged() {
        assertThat(follow("abc", "abc"), is(new int[]{1, 2, 3}));
    }

    @Test
    public void testTyped() {
        assertThat(follow("abc", "abXc"), is(new int[]{1, 2, 0, 3}));
        assertThat(follow("abc", "Xabc"), is(new int[]{0, 1, 2, 3}));
        assertThat(follow("abc", "abcX"), is(new int[]{1, 2, 3, 0}));
    }

    @Test
    public void testDeleted() {
        assertThat(follow("abcd", "ad"), is(new int[]{1, 4}));
        assertThat(follow("abc", ""), is(new int[]{}));
    }

    @Test
    public void testReplaced() {
        assertThat(follow("a + b", "a * b"), is(new int[]{1, 2, 0, 4, 5}));
        assertThat(follow("abc", "xyz"), is(new int[]{0, 0, 0}));
    }

    @Test
    public void testRepeatedCharacters() {
        // The kept text before and after a change never overlaps
        assertThat(follow("aa", "aaa"), is(new int[]{1, 2, 0}));
        assertThat(follow("aaa", "aa"), is(new int[]{1, 2}));
    }

}
