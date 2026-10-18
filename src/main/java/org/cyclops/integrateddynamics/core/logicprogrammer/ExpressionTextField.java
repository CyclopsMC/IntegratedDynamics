package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParser;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * The text of an expression in the logic programmer, which can be edited in place.
 * The text is either the printed expression, text that was applied as typed and is printed once it loses focus,
 * or a draft that the player typed and that is not applied yet.
 * It is drawn with a color for each character, which vanilla text boxes cannot do.
 * It is kept in the container, so it survives the panel being recreated, such as when navigating.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ExpressionTextField {

    public static final int LINE_HEIGHT = 9;
    private static final int COLOR_SELECTION = 0xFF3060A0;
    private static final int COLOR_CARET = 0xFFE0E0E0;
    private static final int COLOR_SCROLLBAR = 0xFF808080;

    private final Font font;
    private final TextModel text;
    private List<ExpressionPrinter.Token> tokens = List.of();
    private String printed = "";
    @Nullable
    private Expression printedExpression = null;
    // The text that the expression was applied from, kept as typed while it has focus
    @Nullable
    private String applied = null;
    // The applied expression, until it is followed
    @Nullable
    private Expression appliedExpression = null;
    private boolean focused = false;
    // Pressed in the text, so dragging selects, also after the panel was recreated by the press
    private boolean pressed = false;
    private int scroll = 0;
    private int visibleLines = 1;
    private long lastInput = 0;
    private long lastEdit = 0;

    /**
     * @param font The font.
     * @param width The width at which lines wrap.
     */
    public ExpressionTextField(Font font, int width) {
        this.font = font;
        this.text = new TextModel(font, width);
        // One more than can be parsed, so pasting too long text gives an error instead of being cut off unnoticed
        this.text.setCharacterLimit(ExpressionParser.MAX_LENGTH + 1);
        this.text.setCursorListener(this::scrollToCursor);
    }

    public String getValue() {
        return text.value();
    }

    /**
     * @return If the text was typed, differs from the expression and is not applied yet.
     */
    public boolean isDraft() {
        return !isPrinted() && !text.value().equals(applied);
    }

    /**
     * @return If the text is the printed expression, so the tokens tell the node of each character.
     */
    public boolean isPrinted() {
        return text.value().equals(printed);
    }

    /**
     * Mark the text as applied, which is no draft anymore and keeps it as typed while it has focus.
     * @param expression The expression it is applied as, or null if it is the current one.
     */
    public void markApplied(@Nullable Expression expression) {
        applied = text.value();
        appliedExpression = expression;
    }

    /**
     * Print the expression in place of applied text, unless the applied expression is not followed yet.
     */
    public void format() {
        if (!isDraft() && !isPrinted() && appliedExpression == null) {
            revert();
        }
    }

    /**
     * @return The time since the text was last changed by typing.
     */
    public long getMillisSinceEdit() {
        return Util.getMillis() - lastEdit;
    }

    /**
     * @return The tokens of the printed expression, which tell the node of each character while it is printed.
     */
    public List<ExpressionPrinter.Token> getTokens() {
        return tokens;
    }

    /**
     * @return The text of the expression, without a draft.
     */
    public String getPrinted() {
        return printed;
    }

    /**
     * @param values A value for each character of the printed text, such as its color.
     * @param typed The value of the characters that were typed.
     * @return A value for each character of the text, where the text before and after what was typed keeps its value.
     */
    public int[] followDraft(int[] values, int typed) {
        return followChanges(printed, text.value(), values, typed);
    }

    /**
     * @param from A text.
     * @param to The text after a change.
     * @param values A value for each character of the first text.
     * @param changed The value of the characters that changed.
     * @return A value for each character of the second text, which is the value of the same character in the first text
     *         for the characters before and after the change.
     */
    public static int[] followChanges(String from, String to, int[] values, int changed) {
        int length = Math.min(from.length(), to.length());
        int prefix = 0;
        while (prefix < length && from.charAt(prefix) == to.charAt(prefix)) {
            prefix++;
        }
        int suffix = 0;
        while (suffix < length - prefix && from.charAt(from.length() - 1 - suffix) == to.charAt(to.length() - 1 - suffix)) {
            suffix++;
        }
        int[] result = new int[to.length()];
        Arrays.fill(result, changed);
        System.arraycopy(values, 0, result, 0, prefix);
        System.arraycopy(values, from.length() - suffix, result, to.length() - suffix, suffix);
        return result;
    }

    /**
     * Follow the given expression. A draft is kept, and is compared to the new expression from now on.
     * Applied text is kept if this is the expression it was applied as, and is printed otherwise.
     * @param expression The current expression.
     * @param printer Prints the expression as tokens, only called when the expression changed.
     */
    public void sync(Expression expression, Supplier<List<ExpressionPrinter.Token>> printer) {
        if (expression == printedExpression) {
            return;
        }
        boolean draft = isDraft();
        printedExpression = expression;
        tokens = printer.get();
        StringBuilder sb = new StringBuilder();
        tokens.forEach(token -> sb.append(token.text()));
        printed = sb.toString();
        if (appliedExpression != null && appliedExpression.equals(expression)) {
            appliedExpression = null;
        } else if (!draft) {
            revert();
        }
    }

    /**
     * Throw away the draft or the applied text, so the text is the printed expression.
     */
    public void revert() {
        applied = null;
        appliedExpression = null;
        setValueKeepCursor(printed);
    }

    /**
     * Forget everything, such as when another expression is opened.
     */
    public void reset() {
        printedExpression = null;
        tokens = List.of();
        printed = "";
        applied = null;
        appliedExpression = null;
        text.setValue("");
        focused = false;
        pressed = false;
        scroll = 0;
    }

    private void setValueKeepCursor(String value) {
        int cursor = text.cursor();
        text.setValue(value);
        text.seekCursor(Whence.ABSOLUTE, Math.min(cursor, value.length()));
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        this.lastInput = Util.getMillis();
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public int getCursor() {
        return text.cursor();
    }

    /**
     * @return The index of the character right after the cursor, or right before it at the end of a line.
     */
    public int getCharAtCursor() {
        String value = text.value();
        int cursor = text.cursor();
        return cursor < value.length() && value.charAt(cursor) != '\n' ? cursor : cursor - 1;
    }

    // --------------- Input ---------------

    /**
     * Place the cursor, or extend the selection while shift is held.
     * @param x The x position, relative to the text.
     * @param y The y position, relative to the text.
     */
    public void mouseClicked(double x, double y) {
        text.setSelecting(Screen.hasShiftDown());
        seek(x, y);
    }

    /**
     * Select from the cursor to the given position.
     * @param x The x position, relative to the text.
     * @param y The y position, relative to the text.
     */
    public void mouseDragged(double x, double y) {
        text.setSelecting(true);
        seek(x, y);
        text.setSelecting(Screen.hasShiftDown());
    }

    private void seek(double x, double y) {
        lastInput = Util.getMillis();
        text.seekCursorToPoint(x, y + scroll * LINE_HEIGHT);
    }

    public void scroll(double amount) {
        scroll = Mth.clamp(scroll - (int) Math.signum(amount), 0, getMaxScroll());
    }

    /**
     * Handle a key such as an arrow, backspace, or a shortcut to select, copy, cut or paste.
     * @param keyCode The key.
     * @return If it was handled.
     */
    public boolean keyPressed(int keyCode) {
        lastInput = Util.getMillis();
        String value = text.value();
        boolean handled = text.keyPressed(keyCode);
        if (!value.equals(text.value())) {
            lastEdit = lastInput;
        }
        return handled;
    }

    public void charTyped(char character) {
        if (StringUtil.isAllowedChatCharacter(character)) {
            insert(Character.toString(character));
        }
    }

    public void insert(String value) {
        lastInput = Util.getMillis();
        lastEdit = lastInput;
        text.insertText(value);
    }

    private int getMaxScroll() {
        return Math.max(0, text.getLineCount() - visibleLines);
    }

    private void scrollToCursor() {
        int line = text.getLineAtCursor();
        if (line < scroll) {
            scroll = line;
        } else if (line >= scroll + visibleLines) {
            scroll = line - visibleLines + 1;
        }
    }

    // --------------- Rendering ---------------

    /**
     * @param guiGraphics The graphics.
     * @param x The left of the text.
     * @param y The top of the text.
     * @param width The width of the text, including room for a scrollbar.
     * @param height The height of the text.
     * @param colors The color of each character.
     * @param backgrounds The background of each character, or 0 for none.
     * @param mark A character to underline, such as where an error is, or -1.
     * @param markColor The color of the underline.
     */
    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int[] colors, int[] backgrounds,
                       int mark, int markColor) {
        visibleLines = Math.max(1, height / LINE_HEIGHT);
        scroll = Mth.clamp(scroll, 0, getMaxScroll());
        String value = text.value();
        int[] selection = text.getSelection();
        int[] lines = text.getLines();
        for (int line = 0; line < lines.length / 2; line++) {
            if (line >= scroll && line < scroll + visibleLines) {
                int lineY = y + (line - scroll) * LINE_HEIGHT;
                int begin = lines[line * 2];
                int end = lines[line * 2 + 1];
                // Backgrounds, with the selection on top
                for (int i = begin; i < end; i++) {
                    boolean selected = selection != null && i >= selection[0] && i < selection[1];
                    int background = selected ? COLOR_SELECTION : backgrounds[i];
                    if (background != 0) {
                        int charX = x + font.width(value.substring(begin, i));
                        guiGraphics.fill(charX, lineY - 1, charX + font.width(value.substring(i, i + 1)), lineY + LINE_HEIGHT - 1, background);
                    }
                }
                // Runs of characters with the same color
                int runStart = begin;
                int runX = x;
                for (int i = begin; i <= end; i++) {
                    if (i == end || colors[i] != colors[runStart]) {
                        if (i > runStart) {
                            String run = value.substring(runStart, i);
                            guiGraphics.drawString(font, run, runX, lineY, colors[runStart], false);
                            runX += font.width(run);
                        }
                        runStart = i;
                    }
                }
                if (mark >= begin && (mark < end || (mark == end && end == value.length()))) {
                    int markX = x + font.width(value.substring(begin, mark));
                    int markWidth = mark < end ? font.width(value.substring(mark, mark + 1)) : 4;
                    guiGraphics.fill(markX, lineY + LINE_HEIGHT - 1, markX + Math.max(markWidth, 2), lineY + LINE_HEIGHT, markColor);
                }
            }
        }

        // Caret, blinking
        int cursorLine = text.getLineAtCursor();
        if (focused && (Util.getMillis() - lastInput) / 500 % 2 == 0 && cursorLine >= scroll && cursorLine < scroll + visibleLines) {
            int lineBegin = lines[cursorLine * 2];
            int cursor = Mth.clamp(text.cursor(), lineBegin, lines[cursorLine * 2 + 1]);
            int caretX = x + font.width(value.substring(lineBegin, cursor));
            int caretY = y + (cursorLine - scroll) * LINE_HEIGHT;
            guiGraphics.fill(caretX, caretY - 1, caretX + 1, caretY + LINE_HEIGHT - 1, COLOR_CARET);
        }

        // Scrollbar
        int lineCount = text.getLineCount();
        if (lineCount > visibleLines) {
            int barHeight = Math.max(4, height * visibleLines / lineCount);
            int barY = y + (height - barHeight) * scroll / getMaxScroll();
            guiGraphics.fill(x + width - 2, barY, x + width, barY + barHeight, COLOR_SCROLLBAR);
        }
    }

    /**
     * Vanilla's text model, with its lines as plain indexes, since its line type is not visible here.
     */
    private static class TextModel extends MultilineTextField {

        public TextModel(Font font, int width) {
            super(font, width);
        }

        /**
         * @return The begin and end index of each line, after each other.
         */
        public int[] getLines() {
            int[] lines = new int[getLineCount() * 2];
            int line = 0;
            for (StringView view : iterateLines()) {
                lines[line * 2] = view.beginIndex();
                lines[line * 2 + 1] = view.endIndex();
                line++;
            }
            return lines;
        }

        /**
         * @return The begin and end index of the selection, or null if nothing is selected.
         */
        @Nullable
        public int[] getSelection() {
            if (!hasSelection()) {
                return null;
            }
            StringView selected = getSelected();
            return new int[]{selected.beginIndex(), selected.endIndex()};
        }
    }

}
