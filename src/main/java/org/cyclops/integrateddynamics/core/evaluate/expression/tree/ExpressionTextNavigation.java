package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Words and brackets in the text of expressions, for moving through it and selecting parts of it.
 *
 * Words end where names, numbers, symbols and spaces meet, so {@code item.size()} has the words
 * {@code item}, {@code .}, {@code size}, {@code (} and {@code )}, where vanilla text fields only stop at spaces.
 * Each bracket is a word of its own.
 *
 * @author rubensworks
 */
public final class ExpressionTextNavigation {

    private static final String OPENING = "([{";
    private static final String CLOSING = ")]}";

    private ExpressionTextNavigation() {
    }

    private static enum Kind {
        SPACE,
        NAME,
        BRACKET,
        SYMBOL
    }

    private static Kind getKind(String text, int index) {
        char c = text.charAt(index);
        if (ExpressionParser.isWhitespace(c)) {
            return Kind.SPACE;
        }
        // The point of a decimal number is part of it, as in 1.5
        if (Character.isLetterOrDigit(c) || c == '_' || (c == '.' && index > 0 && index + 1 < text.length()
                && Character.isDigit(text.charAt(index - 1)) && Character.isDigit(text.charAt(index + 1)))) {
            return Kind.NAME;
        }
        return OPENING.indexOf(c) >= 0 || CLOSING.indexOf(c) >= 0 ? Kind.BRACKET : Kind.SYMBOL;
    }

    /**
     * @param text A text.
     * @param index The index of a character.
     * @return The begin and end index of the word that contains the character, such as for a double-click.
     */
    public static int[] getWordAt(String text, int index) {
        if (text.isEmpty()) {
            return new int[]{0, 0};
        }
        index = Math.max(0, Math.min(index, text.length() - 1));
        Kind kind = getKind(text, index);
        if (kind == Kind.BRACKET) {
            return new int[]{index, index + 1};
        }
        int begin = index;
        while (begin > 0 && getKind(text, begin - 1) == kind) {
            begin--;
        }
        int end = index + 1;
        while (end < text.length() && getKind(text, end) == kind) {
            end++;
        }
        return new int[]{begin, end};
    }

    /**
     * @param text A text.
     * @param cursor The position of a cursor.
     * @return The end of the next word after the cursor, skipping spaces.
     */
    public static int getNextWordEnd(String text, int cursor) {
        int i = Math.max(0, Math.min(cursor, text.length()));
        while (i < text.length() && getKind(text, i) == Kind.SPACE) {
            i++;
        }
        if (i < text.length()) {
            i = getWordAt(text, i)[1];
        }
        return i;
    }

    /**
     * @param text A text.
     * @param cursor The position of a cursor.
     * @return The start of the previous word before the cursor, skipping spaces.
     */
    public static int getPreviousWordStart(String text, int cursor) {
        int i = Math.max(0, Math.min(cursor, text.length()));
        while (i > 0 && getKind(text, i - 1) == Kind.SPACE) {
            i--;
        }
        if (i > 0) {
            i = getWordAt(text, i - 1)[0];
        }
        return i;
    }

    /**
     * @param text A text.
     * @return For each character, the index of the bracket it is matched with, or -1 if it is no matched bracket.
     *         Brackets in strings are skipped.
     */
    public static int[] getBracketPartners(String text) {
        int[] partners = new int[text.length()];
        Arrays.fill(partners, -1);
        Deque<Integer> open = new ArrayDeque<>();
        boolean inString = false;
        boolean singleQuoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (singleQuoted ? ExpressionParser.isSingleQuote(c) : ExpressionParser.isQuote(c)) {
                    inString = false;
                }
            } else if (ExpressionParser.isQuote(c) || ExpressionParser.isSingleQuote(c)) {
                inString = true;
                singleQuoted = ExpressionParser.isSingleQuote(c);
            } else if (OPENING.indexOf(c) >= 0) {
                open.push(i);
            } else if (CLOSING.indexOf(c) >= 0 && !open.isEmpty()
                    && OPENING.indexOf(text.charAt(open.peek())) == CLOSING.indexOf(c)) {
                int partner = open.pop();
                partners[i] = partner;
                partners[partner] = i;
            }
        }
        return partners;
    }

    /**
     * @param text A text.
     * @param cursor The position of a cursor.
     * @return The indexes of the bracket next to the cursor and the bracket it is matched with, or null if there is none.
     *         The bracket before the cursor goes first, as it was usually just typed.
     */
    @Nullable
    public static int[] getMatchingBrackets(String text, int cursor) {
        int[] partners = getBracketPartners(text);
        for (int index : new int[]{cursor - 1, cursor}) {
            if (index >= 0 && index < text.length() && partners[index] >= 0) {
                return new int[]{index, partners[index]};
            }
        }
        return null;
    }

    /**
     * Where a key moves the cursor to, or deletes up to, for the keys where expressions differ from vanilla's text fields.
     * Ctrl, or Option on macOS, moves and deletes by word. On macOS, Cmd moves and deletes to the start or end of the line,
     * and Cmd with Up or Down to the start or end of the text, as in its own text fields.
     * @param text A text.
     * @param cursor The position of the cursor.
     * @param lineBegin The begin of the line of the cursor.
     * @param lineEnd The end of the line of the cursor.
     * @param key The key.
     * @param osx If this runs on macOS.
     * @param control If Ctrl is held, or Cmd on macOS, as vanilla's {@code Screen.hasControlDown} tells.
     * @param alt If Alt is held, which is Option on macOS.
     * @return Where to, or null if the key works as in vanilla's text fields.
     */
    @Nullable
    public static KeyTarget getKeyTarget(String text, int cursor, int lineBegin, int lineEnd, int key,
                                         boolean osx, boolean control, boolean alt) {
        if (osx && control) {
            return switch (key) {
                case GLFW.GLFW_KEY_LEFT -> new KeyTarget(lineBegin, false);
                case GLFW.GLFW_KEY_RIGHT -> new KeyTarget(lineEnd, false);
                case GLFW.GLFW_KEY_UP -> new KeyTarget(0, false);
                case GLFW.GLFW_KEY_DOWN -> new KeyTarget(text.length(), false);
                case GLFW.GLFW_KEY_BACKSPACE -> new KeyTarget(lineBegin, true);
                case GLFW.GLFW_KEY_DELETE -> new KeyTarget(lineEnd, true);
                default -> null;
            };
        }
        if (osx ? alt : control) {
            return switch (key) {
                case GLFW.GLFW_KEY_LEFT -> new KeyTarget(getPreviousWordStart(text, cursor), false);
                case GLFW.GLFW_KEY_RIGHT -> new KeyTarget(getNextWordEnd(text, cursor), false);
                case GLFW.GLFW_KEY_BACKSPACE -> new KeyTarget(getPreviousWordStart(text, cursor), true);
                case GLFW.GLFW_KEY_DELETE -> new KeyTarget(getNextWordEnd(text, cursor), true);
                default -> null;
            };
        }
        return null;
    }

    /**
     * @param position Where to move the cursor to, or delete up to.
     * @param delete If the text up to there is deleted.
     */
    public static record KeyTarget(int position, boolean delete) {}

}
