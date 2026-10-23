package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import org.junit.Test;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for words and brackets in the text of expressions.
 * @author rubensworks
 */
public class TestExpressionTextNavigation {

    private static String wordAt(String text, int index) {
        int[] word = ExpressionTextNavigation.getWordAt(text, index);
        return text.substring(word[0], word[1]);
    }

    @Test
    public void testWordAt() {
        String text = "item.size() >= 16";
        assertThat(wordAt(text, 1), is("item"));
        assertThat(wordAt(text, 3), is("item"));
        assertThat(wordAt(text, 4), is("."));
        assertThat(wordAt(text, 6), is("size"));
        assertThat(wordAt(text, 9), is("("));
        assertThat(wordAt(text, 10), is(")"));
        assertThat(wordAt(text, 11), is(" "));
        assertThat(wordAt(text, 13), is(">="));
        assertThat(wordAt(text, 16), is("16"));
        // Numbers with a point, labels, typed values and strings
        assertThat(wordAt("x * 1.5 + 2", 5), is("1.5"));
        assertThat(wordAt("#my_label + 1", 4), is("my_label"));
        assertThat(wordAt("@item{id: \"minecraft:diamond\"}", 3), is("item"));
        assertThat(wordAt("\"hello world\" == ?", 3), is("hello"));
        // Past the end, and no text
        assertThat(wordAt(text, 100), is("16"));
        assertThat(ExpressionTextNavigation.getWordAt("", 0), is(new int[]{0, 0}));
    }

    private static List<String> walk(String text, boolean forward) {
        List<String> stops = new ArrayList<>();
        int cursor = forward ? 0 : text.length();
        while (forward ? cursor < text.length() : cursor > 0) {
            int next = forward ? ExpressionTextNavigation.getNextWordEnd(text, cursor) : ExpressionTextNavigation.getPreviousWordStart(text, cursor);
            stops.add(forward ? text.substring(0, next) : text.substring(next));
            cursor = next;
        }
        return stops;
    }

    @Test
    public void testWordSteps() {
        String text = "item.size(x) >= 16";
        assertThat(walk(text, true), is(List.of("item", "item.", "item.size", "item.size(", "item.size(x", "item.size(x)",
                "item.size(x) >=", "item.size(x) >= 16")));
        assertThat(walk(text, false), is(List.of("16", ">= 16", ") >= 16", "x) >= 16", "(x) >= 16", "size(x) >= 16",
                ".size(x) >= 16", "item.size(x) >= 16")));
        // Spaces and line breaks are skipped
        assertThat(ExpressionTextNavigation.getNextWordEnd("a  \n  b", 1), is(7));
        assertThat(ExpressionTextNavigation.getPreviousWordStart("a  \n  b", 6), is(0));
        assertThat(ExpressionTextNavigation.getNextWordEnd("a", 5), is(1));
        assertThat(ExpressionTextNavigation.getPreviousWordStart("", 0), is(0));
    }

    @Test
    public void testBrackets() {
        String text = "f(a, (b + c), [1, 2])";
        int[] partners = ExpressionTextNavigation.getBracketPartners(text);
        assertThat(partners[1], is(20));
        assertThat(partners[20], is(1));
        assertThat(partners[5], is(11));
        assertThat(partners[14], is(19));
        assertThat(partners[0], is(-1));
        // Brackets in strings, also single quoted and escaped, do not count
        assertThat(ExpressionTextNavigation.getBracketPartners("(\")\")")[0], is(4));
        assertThat(ExpressionTextNavigation.getBracketPartners("('it\\'s )')")[0], is(10));
        assertThat(ExpressionTextNavigation.getBracketPartners("(\"it's\")")[0], is(7));
        // Braces of typed values
        assertThat(ExpressionTextNavigation.getBracketPartners("@nbt{a: [1]}")[4], is(11));
        // Brackets that do not match
        assertThat(ExpressionTextNavigation.getBracketPartners("(]")[0], is(-1));
        assertThat(ExpressionTextNavigation.getBracketPartners("(a")[0], is(-1));
    }

    @Test
    public void testMatchingBrackets() {
        // The bracket before the cursor goes first
        assertThat(ExpressionTextNavigation.getMatchingBrackets("(a)(b)", 3), is(new int[]{2, 0}));
        assertThat(ExpressionTextNavigation.getMatchingBrackets("(a)(b)", 0), is(new int[]{0, 2}));
        assertThat(ExpressionTextNavigation.getMatchingBrackets("(a)(b)", 6), is(new int[]{5, 3}));
        assertThat(ExpressionTextNavigation.getMatchingBrackets("(ab)", 2), nullValue());
        assertThat(ExpressionTextNavigation.getMatchingBrackets("(a", 1), nullValue());
    }

    private static ExpressionTextNavigation.KeyTarget target(int key, boolean osx, boolean control, boolean alt) {
        // The cursor is at 9, in "item.size() + 1" on a line from 0 to 15
        return ExpressionTextNavigation.getKeyTarget("item.size() + 1", 9, 0, 15, key, osx, control, alt);
    }

    @Test
    public void testKeysOtherPlatforms() {
        assertThat(target(GLFW.GLFW_KEY_LEFT, false, true, false), is(new ExpressionTextNavigation.KeyTarget(5, false)));
        assertThat(target(GLFW.GLFW_KEY_RIGHT, false, true, false), is(new ExpressionTextNavigation.KeyTarget(10, false)));
        assertThat(target(GLFW.GLFW_KEY_BACKSPACE, false, true, false), is(new ExpressionTextNavigation.KeyTarget(5, true)));
        assertThat(target(GLFW.GLFW_KEY_DELETE, false, true, false), is(new ExpressionTextNavigation.KeyTarget(10, true)));
        // Alt, Up and Down, and keys without modifiers work as in vanilla
        assertThat(target(GLFW.GLFW_KEY_LEFT, false, false, true), nullValue());
        assertThat(target(GLFW.GLFW_KEY_UP, false, true, false), nullValue());
        assertThat(target(GLFW.GLFW_KEY_LEFT, false, false, false), nullValue());
    }

    @Test
    public void testKeysMacOs() {
        // Option by word
        assertThat(target(GLFW.GLFW_KEY_LEFT, true, false, true), is(new ExpressionTextNavigation.KeyTarget(5, false)));
        assertThat(target(GLFW.GLFW_KEY_RIGHT, true, false, true), is(new ExpressionTextNavigation.KeyTarget(10, false)));
        assertThat(target(GLFW.GLFW_KEY_BACKSPACE, true, false, true), is(new ExpressionTextNavigation.KeyTarget(5, true)));
        assertThat(target(GLFW.GLFW_KEY_DELETE, true, false, true), is(new ExpressionTextNavigation.KeyTarget(10, true)));
        // Cmd to the line, and with Up and Down to the text, also together with Option
        assertThat(target(GLFW.GLFW_KEY_LEFT, true, true, false), is(new ExpressionTextNavigation.KeyTarget(0, false)));
        assertThat(target(GLFW.GLFW_KEY_RIGHT, true, true, true), is(new ExpressionTextNavigation.KeyTarget(15, false)));
        assertThat(target(GLFW.GLFW_KEY_UP, true, true, false), is(new ExpressionTextNavigation.KeyTarget(0, false)));
        assertThat(target(GLFW.GLFW_KEY_DOWN, true, true, false), is(new ExpressionTextNavigation.KeyTarget(15, false)));
        assertThat(target(GLFW.GLFW_KEY_BACKSPACE, true, true, false), is(new ExpressionTextNavigation.KeyTarget(0, true)));
        assertThat(target(GLFW.GLFW_KEY_DELETE, true, true, false), is(new ExpressionTextNavigation.KeyTarget(15, true)));
        // Without modifiers as in vanilla
        assertThat(target(GLFW.GLFW_KEY_LEFT, true, false, false), nullValue());
        assertThat(target(GLFW.GLFW_KEY_HOME, true, true, false), nullValue());
    }

}
