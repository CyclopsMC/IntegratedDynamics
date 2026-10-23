package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParser;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTextNavigation;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.OperatorNames;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The text of an expression in the logic programmer, which can be edited in place.
 * The text is either the text of the expression, which is the text it was typed as or otherwise its printed form,
 * text that was applied and is not followed yet, or a draft that the player typed and that is not applied yet.
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
    private static final int COLOR_BRACKET = 0xFF2F6B60;
    private static final long MULTI_CLICK_MS = 250;
    private static final int COMPLETION_ROWS = 6;
    private static final int COLOR_POPUP = 0xFF202020;
    private static final int COLOR_POPUP_BORDER = 0xFFA0A0A0;
    private static final int COLOR_DETAIL = 0xFF909090;
    // Neutral, so the colors of types stay readable
    private static final int COLOR_POPUP_SELECTED = 0xFF505050;
    // Height of the arrow between an error tooltip and its error, as in Integrated Scripting's editor
    private static final int TOOLTIP_ARROW = 4;
    // How far vanilla tooltip backgrounds extend beyond their contents, and their colors
    private static final int TOOLTIP_BORDER = 4;
    private static final int TOOLTIP_BACKGROUND = 0xF0100010;
    private static final int TOOLTIP_BORDER_COLOR = 0x505000FF;
    private static final int TOOLTIP_WIDTH = 200;

    private final Font font;
    private final TextModel text;
    private List<ExpressionPrinter.Token> tokens = List.of();
    private String printed = "";
    @Nullable
    private Expression printedExpression = null;
    @Nullable
    private String printedText = null;
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
    private long lastClick = 0;
    private int lastClickIndex = -1;
    // Selected by a double or triple click, which a drag that stays on the clicked character keeps
    private boolean selectedByClicks = false;
    @Nullable
    private Supplier<ExpressionParser.Context> parserContext = null;
    // What can be completed at the cursor, for the text and cursor it was found for
    private ExpressionParser.Completion completion = ExpressionParser.Completion.NONE;
    @Nullable
    private String completionText = null;
    private int completionCursor = -1;
    // The methods or types shown while typing their name, for the text and cursor they were found for
    private List<Completion> completions = List.of();
    @Nullable
    private String completionsText = null;
    private int completionsCursor = -1;
    private int completionsStart = 0;
    private int completionIndex = 0;
    // Where the list is drawn, relative to the text, the first entry drawn, and where the text is drawn
    @Nullable
    private int[] completionsBox = null;
    private int completionsFirst = 0;
    private int textX = 0;
    private int textY = 0;
    private int textWidth = 0;
    // If the text was drawn since the list was last drawn, as it is drawn after everything else
    private boolean completionsPlaced = false;
    // The signature of the call at the cursor, and where it is drawn, relative to the text
    @Nullable
    private Component signature = null;
    @Nullable
    private int[] signatureBox = null;

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
     * @return If the text is the text of the expression, so the tokens tell the node of each character.
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
     * Show the text of the expression in place of applied text, unless the applied expression is not followed yet.
     * The expression keeps the text it was typed as, so this only changes text that no longer reads as the expression,
     * such as a reference whose type became known.
     */
    public void showExpressionText() {
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
     * @return The tokens of the text of the expression, which tell the node of each character while it is shown.
     */
    public List<ExpressionPrinter.Token> getTokens() {
        return tokens;
    }

    /**
     * @return The text of the expression, as typed or printed, without a draft.
     */
    public String getPrinted() {
        return printed;
    }

    /**
     * @param values A value for each character of the text of the expression, such as its color.
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
     * Applied text is kept if this is the expression it was applied as, and is replaced by the text of the expression otherwise.
     * @param expression The current expression.
     * @param text The text it was typed as, or null if it is printed.
     * @param printer Gives the text of the expression as tokens, only called when the expression or its text changed.
     */
    public void sync(Expression expression, @Nullable String text, Supplier<List<ExpressionPrinter.Token>> printer) {
        if (expression == printedExpression && Objects.equals(text, printedText)) {
            return;
        }
        boolean draft = isDraft();
        printedExpression = expression;
        printedText = text;
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
     * Throw away the draft or the applied text, so the text is that of the expression.
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
        printedText = null;
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
        if (!focused) {
            closeCompletions();
        }
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
     * A double click selects a word, and a triple click everything.
     * @param x The x position, relative to the text.
     * @param y The y position, relative to the text.
     */
    public void mouseClicked(double x, double y) {
        closeCompletions();
        long now = Util.getMillis();
        int index = getIndexAtPoint(x, y);
        boolean repeated = now - lastClick < MULTI_CLICK_MS && index == lastClickIndex && !Screen.hasShiftDown();
        lastClick = now;
        lastClickIndex = index;
        selectedByClicks = repeated;
        if (repeated) {
            lastInput = now;
            if (text.hasSelection()) {
                select(0, text.value().length());
            } else {
                int[] word = ExpressionTextNavigation.getWordAt(text.value(), index);
                select(word[0], word[1]);
            }
            return;
        }
        text.setSelecting(Screen.hasShiftDown());
        seek(x, y);
    }

    /**
     * Select from the cursor to the given position.
     * @param x The x position, relative to the text.
     * @param y The y position, relative to the text.
     */
    public void mouseDragged(double x, double y) {
        if (selectedByClicks && getIndexAtPoint(x, y) == lastClickIndex) {
            return;
        }
        text.setSelecting(true);
        seek(x, y);
        text.setSelecting(Screen.hasShiftDown());
    }

    private void select(int begin, int end) {
        text.setSelecting(false);
        text.seekCursor(Whence.ABSOLUTE, begin);
        text.setSelecting(true);
        text.seekCursor(Whence.ABSOLUTE, end);
        text.setSelecting(false);
    }

    /**
     * @param x The x position, relative to the text.
     * @param y The y position, relative to the text.
     * @return The index of the character at the position, or of the last one of its line when the position is after it.
     */
    private int getIndexAtPoint(double x, double y) {
        String value = text.value();
        int[] lines = text.getLines();
        int line = Mth.clamp(Mth.floor(y / LINE_HEIGHT) + scroll, 0, lines.length / 2 - 1);
        int begin = lines[line * 2];
        int end = lines[line * 2 + 1];
        int charRight = 0;
        for (int i = begin; i < end; i++) {
            charRight += font.width(value.substring(i, i + 1));
            if (x < charRight) {
                return i;
            }
        }
        return Math.max(begin, end - 1);
    }

    private void seek(double x, double y) {
        lastInput = Util.getMillis();
        text.seekCursor(Whence.ABSOLUTE, getCursorAtPoint(x, y));
    }

    /**
     * @param x The x position, relative to the text.
     * @param y The y position, relative to the text.
     * @return The position between characters that is closest to the point, where vanilla's text model takes the one
     *         before the character under it, which makes it hard to click between two brackets.
     */
    private int getCursorAtPoint(double x, double y) {
        String value = text.value();
        int[] lines = text.getLines();
        int line = Mth.clamp(Mth.floor(y / LINE_HEIGHT) + scroll, 0, lines.length / 2 - 1);
        int end = lines[line * 2 + 1];
        int charLeft = 0;
        for (int i = lines[line * 2]; i < end; i++) {
            int width = font.width(value.substring(i, i + 1));
            if (x < charLeft + width / 2.0) {
                return i;
            }
            charLeft += width;
        }
        return end;
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
        boolean handled = keyPressedNavigation(keyCode) || text.keyPressed(keyCode);
        if (!value.equals(text.value())) {
            lastEdit = lastInput;
            // Such as backspace while typing a method name
            updateCompletions();
        } else {
            closeCompletions();
        }
        return handled;
    }

    // Moving and deleting by word, and by line on macOS, where expressions differ from vanilla's text fields
    private boolean keyPressedNavigation(int keyCode) {
        int[] lines = text.getLines();
        int line = Mth.clamp(text.getLineAtCursor(), 0, lines.length / 2 - 1);
        ExpressionTextNavigation.KeyTarget target = ExpressionTextNavigation.getKeyTarget(text.value(), text.cursor(),
                lines[line * 2], lines[line * 2 + 1], keyCode, Minecraft.ON_OSX, Screen.hasControlDown(), Screen.hasAltDown());
        if (target == null) {
            return false;
        }
        if (target.delete()) {
            // Deletes the selection instead, if any
            text.deleteText(target.position() - text.cursor());
        } else {
            text.setSelecting(Screen.hasShiftDown());
            text.seekCursor(Whence.ABSOLUTE, target.position());
        }
        return true;
    }

    public void charTyped(char character) {
        if (StringUtil.isAllowedChatCharacter(character)) {
            insert(Character.toString(character));
            updateCompletions();
        }
    }

    public void insert(String value) {
        lastInput = Util.getMillis();
        lastEdit = lastInput;
        text.insertText(value);
    }

    /**
     * Change the text as if it was typed, with the cursor after the part that changed.
     * @param value The new text.
     */
    public void replace(String value) {
        String old = text.value();
        int length = Math.min(old.length(), value.length());
        int suffix = 0;
        while (suffix < length && old.charAt(old.length() - 1 - suffix) == value.charAt(value.length() - 1 - suffix)) {
            suffix++;
        }
        lastInput = Util.getMillis();
        lastEdit = lastInput;
        text.setValue(value);
        text.seekCursor(Whence.ABSOLUTE, value.length() - suffix);
    }

    /**
     * Add a parameter to the text, as typing it would, with the cursor where the rest is typed if nothing follows the parameters yet.
     * @param context What the parser needs to know about the world.
     */
    public void addParameter(ExpressionParser.Context context) {
        replace(ExpressionEditor.addParameterToText(text.value(), context));
        if (text.value().substring(text.cursor()).matches("\\)?\\s*(=>|->)\\s*")) {
            text.seekCursor(Whence.END, 0);
        }
    }

    // --------------- Completion ---------------

    /**
     * @param parserContext What the parser needs to know about the world, to find what can be completed.
     */
    public void setParserContext(Supplier<ExpressionParser.Context> parserContext) {
        this.parserContext = parserContext;
    }

    private ExpressionParser.Completion getCompletion() {
        String value = text.value();
        int cursor = text.cursor();
        if (cursor != completionCursor || !value.equals(completionText)) {
            completionText = value;
            completionCursor = cursor;
            completion = parserContext == null || text.hasSelection() ? ExpressionParser.Completion.NONE
                    : ExpressionParser.complete(value, cursor, parserContext.get());
        }
        return completion;
    }

    /**
     * @return The call that the cursor is in while the text has focus, or null.
     */
    @Nullable
    private ExpressionParser.Call getCallAtCursor() {
        return focused ? getCompletion().call() : null;
    }

    /**
     * Show the methods whose name starts with what is typed after a point,
     * or the types whose name starts with what is typed after the colon of a parameter or an @, as the text was just typed.
     */
    private void updateCompletions() {
        ExpressionParser.Completion completion = focused ? getCompletion() : ExpressionParser.Completion.NONE;
        List<Completion> items = new ArrayList<>();
        if (completion.methods() != null) {
            ExpressionParser.Methods methods = completion.methods();
            Predicate<IOperator> usable = parserContext.get().operators();
            String prefix = methods.prefix().toLowerCase(Locale.ROOT);
            OperatorNames.getMethods(methods.receiverType()).forEach((name, operator) -> {
                if (name.toLowerCase(Locale.ROOT).startsWith(prefix) && usable.test(operator)) {
                    items.add(new Completion(name, getSignature(operator, true), operator));
                }
            });
            completionsStart = methods.start();
        } else if (completion.types() != null) {
            for (IValueType<?> type : ExpressionParser.getTypes(completion.types(), parserContext.get())) {
                items.add(new Completion(ExpressionParser.getTypeName(type), getTypeName(type), null));
            }
            completionsStart = completion.types().start();
        }
        if (!items.equals(completions)) {
            completionIndex = 0;
        }
        completions = items;
        completionsText = text.value();
        completionsCursor = text.cursor();
    }

    /**
     * @return The entries that are shown, which are hidden once the text or cursor changes in any other way than typing.
     */
    private List<Completion> getShownCompletions() {
        return text.cursor() == completionsCursor && text.value().equals(completionsText) ? completions : List.of();
    }

    /**
     * @return If a list was shown, which is closed now, such as by Escape.
     */
    public boolean closeCompletions() {
        boolean shown = !getShownCompletions().isEmpty();
        completions = List.of();
        return shown;
    }

    /**
     * Handle a key for the shown list: Up and Down to choose an entry, and Tab or Enter to insert it.
     * @param keyCode The key.
     * @return If it was handled.
     */
    public boolean keyPressedCompletion(int keyCode) {
        List<Completion> shown = getShownCompletions();
        if (shown.isEmpty()) {
            return false;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_DOWN -> completionIndex = (completionIndex + 1) % shown.size();
            case GLFW.GLFW_KEY_UP -> completionIndex = (completionIndex + shown.size() - 1) % shown.size();
            case GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> acceptCompletion(completionIndex);
            default -> {
                return false;
            }
        }
        lastInput = Util.getMillis();
        return true;
    }

    /**
     * Insert the clicked entry, if the list is shown there.
     * @param x The x position, relative to the text.
     * @param y The y position, relative to the text.
     * @return If the list was clicked.
     */
    public boolean mouseClickedCompletion(double x, double y) {
        if (!isInCompletions(x, y)) {
            return false;
        }
        int row = Mth.clamp(Mth.floor((y - completionsBox[1] - 1) / LINE_HEIGHT), 0, COMPLETION_ROWS - 1);
        acceptCompletion(Math.min(completionsFirst + row, completions.size() - 1));
        return true;
    }

    /**
     * Move through the shown list, if the mouse is over it.
     * @param x The x position, relative to the text.
     * @param y The y position, relative to the text.
     * @param amount The amount scrolled.
     * @return If the list was scrolled.
     */
    public boolean mouseScrolledCompletion(double x, double y, double amount) {
        if (!isInCompletions(x, y)) {
            return false;
        }
        completionIndex = Mth.clamp(completionIndex - (int) Math.signum(amount), 0, completions.size() - 1);
        return true;
    }

    /**
     * @param mouseX The x position on the screen.
     * @param mouseY The y position on the screen.
     * @return If the shown list or signature is drawn there.
     */
    public boolean isOverPopups(double mouseX, double mouseY) {
        double x = mouseX - textX;
        double y = mouseY - textY;
        return isInCompletions(x, y) || (signatureBox != null && x >= signatureBox[0] - 1 && x < signatureBox[0] + signatureBox[2] + 1
                && y >= signatureBox[1] - 1 && y < signatureBox[1] + signatureBox[3] + 1);
    }

    private boolean isInCompletions(double x, double y) {
        return !getShownCompletions().isEmpty() && completionsBox != null && x >= completionsBox[0] && x < completionsBox[0] + completionsBox[2]
                && y >= completionsBox[1] && y < completionsBox[1] + completionsBox[3];
    }

    /**
     * Replace the typed name with that of a method or type, and the rest of the name after the cursor too.
     * For methods, brackets are added if there are none yet, closed if the method takes no other inputs.
     */
    private void acceptCompletion(int index) {
        Completion completion = completions.get(index);
        completions = List.of();
        String value = text.value();
        int end = text.cursor();
        while (end < value.length() && (completion.operator() == null ? ExpressionParser.isTypeNamePart(value.charAt(end))
                : Character.isLetterOrDigit(value.charAt(end)) || value.charAt(end) == '_')) {
            end++;
        }
        boolean bracket = end < value.length() && value.charAt(end) == '(';
        String brackets = "";
        if (completion.operator() != null && !bracket) {
            brackets = completion.operator().getRequiredInputLength() > 1 ? "(" : "()";
        }
        select(completionsStart, end);
        insert(completion.name() + brackets);
        if (completion.operator() != null && bracket) {
            text.seekCursor(Whence.RELATIVE, 1);
        }
    }

    /**
     * @param operator An operator.
     * @param method If it is called as a method, so its first input is left out.
     * @return Its inputs and output in the colors of their types, as in {@code (Integer, String): Boolean}.
     */
    public static Component getSignature(IOperator operator, boolean method) {
        IValueType<?>[] inputs = operator.getInputTypes();
        MutableComponent signature = Component.literal("(");
        for (int i = method ? Math.min(1, inputs.length) : 0, first = i; i < inputs.length; i++) {
            if (i > first) {
                signature.append(", ");
            }
            signature.append(getTypeName(inputs[i]));
        }
        return signature.append("): ").append(getTypeName(operator.getOutputType()));
    }

    /**
     * @param call A call that the cursor is in.
     * @return Its signature in the colors of its types, with the input at the cursor underlined,
     *         as in {@code Item.strength(Block): Double} for a method, where the first input is before the point.
     */
    public static Component getCallSignature(ExpressionParser.Call call) {
        IOperator operator = call.operator();
        IValueType<?>[] inputs = operator.getInputTypes();
        int first = call.method() ? Math.min(1, inputs.length) : 0;
        MutableComponent signature = Component.empty();
        if (first > 0) {
            signature.append(getTypeName(inputs[0])).append(".");
        }
        signature.append(call.name()).append("(");
        for (int i = first; i < inputs.length; i++) {
            if (i > first) {
                signature.append(", ");
            }
            MutableComponent input = getTypeName(inputs[i]);
            signature.append(i == call.input() ? input.withStyle(ChatFormatting.UNDERLINE) : input);
        }
        return signature.append("): ").append(getTypeName(operator.getOutputType()));
    }

    private static MutableComponent getTypeName(IValueType<?> type) {
        return Component.translatable(type.getTranslationKey()).withStyle(type.getDisplayColorFormat());
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
     * @param mark The first character to underline, such as where an error is, or -1.
     * @param markEnd The character after the last one to underline, which is at least the first one.
     * @param markColor The color of the underline.
     */
    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int[] colors, int[] backgrounds,
                       int mark, int markEnd, int markColor) {
        visibleLines = Math.max(1, height / LINE_HEIGHT);
        scroll = Mth.clamp(scroll, 0, getMaxScroll());
        String value = text.value();
        int[] selection = text.getSelection();
        int[] lines = text.getLines();
        // The bracket next to the cursor and its match
        int[] brackets = focused && selection == null ? ExpressionTextNavigation.getMatchingBrackets(value, text.cursor()) : null;
        for (int line = 0; line < lines.length / 2; line++) {
            if (line >= scroll && line < scroll + visibleLines) {
                int lineY = y + (line - scroll) * LINE_HEIGHT;
                int begin = lines[line * 2];
                int end = lines[line * 2 + 1];
                // Backgrounds, with the selection on top
                for (int i = begin; i < end; i++) {
                    boolean selected = selection != null && i >= selection[0] && i < selection[1];
                    boolean bracket = brackets != null && (i == brackets[0] || i == brackets[1]);
                    int background = selected ? COLOR_SELECTION : bracket ? COLOR_BRACKET : backgrounds[i];
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
                // The marked characters on this line, or a short line after its end, such as at the end of the text
                int markFrom = Math.max(mark, begin);
                int markTo = Math.min(Math.max(markEnd, mark + 1), end);
                if (mark >= 0 && markFrom < markTo) {
                    int markX = x + font.width(value.substring(begin, markFrom));
                    int markWidth = font.width(value.substring(markFrom, markTo));
                    guiGraphics.fill(markX, lineY + LINE_HEIGHT - 1, markX + Math.max(markWidth, 2), lineY + LINE_HEIGHT, markColor);
                } else if (mark == end && mark >= begin && (end == value.length() || value.charAt(end) == '\n')) {
                    int markX = x + font.width(value.substring(begin, end));
                    guiGraphics.fill(markX, lineY + LINE_HEIGHT - 1, markX + 4, lineY + LINE_HEIGHT, markColor);
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

        textX = x;
        textY = y;
        textWidth = width;
        completionsBox = focused && !getShownCompletions().isEmpty() ? placeCompletions(x, y, lines) : null;
        // The signature of the call at the cursor, unless the list is shown, as in Integrated Scripting's editor
        ExpressionParser.Call call = focused && getShownCompletions().isEmpty() ? getCallAtCursor() : null;
        signature = call != null ? getCallSignature(call) : null;
        signatureBox = signature != null && cursorLine >= scroll && cursorLine < scroll + visibleLines
                ? placeSignature(x, y, y + (cursorLine - scroll) * LINE_HEIGHT, font.width(signature),
                        Minecraft.getInstance().getWindow().getGuiScaledWidth()) : null;
        completionsPlaced = true;
    }

    /**
     * Show the message of an error in a tooltip while the mouse is over its line, with an arrow at its position,
     * as in Integrated Scripting's editor. Not while the list is shown, which is what is being typed in,
     * and in place of the signature of the call at the cursor, which it would cover. Called after the text is drawn.
     * @param guiGraphics The graphics.
     * @param position Where the error is.
     * @param end The character after the part with the error, which is at least the position.
     * @param message The message.
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     */
    public void renderErrorTooltip(GuiGraphics guiGraphics, int position, int end, Component message, int mouseX, int mouseY) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null || !getShownCompletions().isEmpty() || isOverPopups(mouseX, mouseY)) {
            return;
        }
        String value = text.value();
        int[] lines = text.getLines();
        position = Mth.clamp(position, 0, value.length());
        // The rows of the line of the error, which can be wrapped over several
        int lineStart = value.lastIndexOf('\n', position - 1) + 1;
        int lineEnd = value.indexOf('\n', position);
        if (lineEnd < 0) {
            lineEnd = value.length();
        }
        boolean hovering = false;
        int errorRow = -1;
        for (int line = scroll; line < Math.min(lines.length / 2, scroll + visibleLines); line++) {
            int begin = lines[line * 2];
            int rowY = textY + (line - scroll) * LINE_HEIGHT;
            if (begin >= lineStart && begin <= lineEnd) {
                hovering |= mouseX >= textX && mouseX < textX + textWidth && mouseY >= rowY - 1 && mouseY < rowY + LINE_HEIGHT - 1;
                if (position >= begin && (position < lines[line * 2 + 1] || position == lines[line * 2 + 1]
                        && (line == lines.length / 2 - 1 || lines[line * 2 + 2] > position))) {
                    errorRow = line;
                }
            }
        }
        if (!hovering) {
            return;
        }
        // Below the row of the error, as the line can be wrapped, or else below the row of the mouse.
        // The arrow points at the middle of the marked part on that row.
        if (errorRow >= 0) {
            int begin = lines[errorRow * 2];
            int rowEnd = Math.min(Math.max(end, position + 1), lines[errorRow * 2 + 1]);
            int width = position < rowEnd ? font.width(value.substring(position, rowEnd)) : 4;
            renderTooltip(guiGraphics, screen, textY + (errorRow - scroll) * LINE_HEIGHT,
                    textX + font.width(value.substring(begin, position)) + width / 2, message, mouseX);
        } else {
            renderTooltip(guiGraphics, screen, textY + Math.floorDiv(mouseY + 1 - textY, LINE_HEIGHT) * LINE_HEIGHT, -1, message, mouseX);
        }
    }

    /**
     * Show the message of an error of a part of the text in a tooltip while the mouse is over that part,
     * with an arrow at the part on the row of the mouse, as for {@link #renderErrorTooltip}.
     * @param guiGraphics The graphics.
     * @param start The first character of the part.
     * @param end The character after the part.
     * @param message The message.
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     */
    public void renderPartTooltip(GuiGraphics guiGraphics, int start, int end, Component message, int mouseX, int mouseY) {
        Screen screen = Minecraft.getInstance().screen;
        int index = getCharAtMouse(mouseX, mouseY);
        if (screen == null || index < start || index >= end || !getShownCompletions().isEmpty() || isOverPopups(mouseX, mouseY)) {
            return;
        }
        String value = text.value();
        int[] lines = text.getLines();
        int line = scroll + Math.floorDiv(mouseY + 1 - textY, LINE_HEIGHT);
        int begin = lines[line * 2];
        int partStart = Math.max(start, begin);
        int partEnd = Math.min(end, lines[line * 2 + 1]);
        int x = textX + font.width(value.substring(begin, partStart)) + font.width(value.substring(partStart, partEnd)) / 2;
        renderTooltip(guiGraphics, screen, textY + (line - scroll) * LINE_HEIGHT, x, message, mouseX);
    }

    /**
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     * @return The index of the character under the mouse, or -1 if it is not over a character of the text.
     */
    public int getCharAtMouse(int mouseX, int mouseY) {
        int x = mouseX - textX;
        // Rows start a pixel above the text, as their backgrounds do
        int row = Math.floorDiv(mouseY + 1 - textY, LINE_HEIGHT);
        int[] lines = text.getLines();
        if (x < 0 || x >= textWidth || row < 0 || row >= visibleLines || scroll + row >= lines.length / 2) {
            return -1;
        }
        String value = text.value();
        int charRight = 0;
        for (int i = lines[(scroll + row) * 2]; i < lines[(scroll + row) * 2 + 1]; i++) {
            charRight += font.width(value.substring(i, i + 1));
            if (x < charRight) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Show a message in a vanilla tooltip below a row, or above it if there is no room, with an arrow at the given x position,
     * in place of the signature of the call at the cursor, which it would cover.
     * @param top The top of the row.
     * @param arrowX Where the arrow points at, or -1 for none, which places the tooltip at the mouse.
     */
    private void renderTooltip(GuiGraphics guiGraphics, Screen screen, int top, int arrowX, Component message, int mouseX) {
        signatureBox = null;
        int bottom = top + LINE_HEIGHT;
        List<FormattedCharSequence> rows = font.split(message, TOOLTIP_WIDTH);
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        // Same height as vanilla tooltips of text lines
        int tooltipHeight = rows.size() * 10 - (rows.size() == 1 ? 2 : 0);
        boolean below = bottom + TOOLTIP_ARROW + TOOLTIP_BORDER + tooltipHeight + TOOLTIP_BORDER <= screenHeight
                || top - TOOLTIP_ARROW - TOOLTIP_BORDER - tooltipHeight - TOOLTIP_BORDER < 0;
        int tooltipY = below ? bottom + TOOLTIP_ARROW + TOOLTIP_BORDER : top - TOOLTIP_ARROW - TOOLTIP_BORDER - tooltipHeight;
        int anchorX = arrowX >= 0 ? arrowX - 12 : mouseX + 12;
        screen.setTooltipForNextRenderPass(rows, (screenWidth, screenHeightIgnored, mouseXIgnored, mouseYIgnored, width, height) ->
                new Vector2i(Math.max(TOOLTIP_BORDER, Math.min(anchorX, screenWidth - width - TOOLTIP_BORDER)), tooltipY), true);
        if (arrowX >= 0) {
            renderTooltipArrow(guiGraphics, arrowX, below ? bottom : top - 1, below);
        }
    }

    /**
     * Draw an arrow between a tooltip and the given position, in the style of vanilla tooltips.
     */
    private static void renderTooltipArrow(GuiGraphics guiGraphics, int x, int tipY, boolean pointUp) {
        guiGraphics.pose().pushPose();
        // Same depth as tooltips
        guiGraphics.pose().translate(0, 0, 400);
        for (int i = 0; i < TOOLTIP_ARROW; i++) {
            int y = pointUp ? tipY + i : tipY - i;
            guiGraphics.fill(x - i, y, x + i + 1, y + 1, TOOLTIP_BACKGROUND);
            guiGraphics.fill(x - i, y, x - i + 1, y + 1, TOOLTIP_BORDER_COLOR);
            guiGraphics.fill(x + i, y, x + i + 1, y + 1, TOOLTIP_BORDER_COLOR);
        }
        guiGraphics.pose().popPose();
    }

    /**
     * Place the signature of a call above the line of the cursor, starting at the left of the text, as in Integrated Scripting's editor.
     * It is placed below the line if there is no room above it within the text, and moved left to stay on the screen.
     * @param x The left of the text.
     * @param y The top of the text.
     * @param lineY The top of the line of the cursor.
     * @param signatureWidth The width of the signature.
     * @param screenWidth The width of the screen.
     * @return The x, y, width and height of the popup within its border, relative to the text.
     */
    static int[] placeSignature(int x, int y, int lineY, int signatureWidth, int screenWidth) {
        int width = Math.min(signatureWidth + 2, screenWidth - 4);
        // A line and a pixel high, which leaves room for the underline of the input at the cursor
        int height = LINE_HEIGHT + 1;
        int boxX = Math.min(x - 1, screenWidth - 2 - width);
        int boxY = lineY - height - 1;
        // The text starts 2 pixels into its box
        if (boxY < y - 2) {
            boxY = lineY + LINE_HEIGHT + 1;
        }
        return new int[]{boxX - x, boxY - y, width, height};
    }

    /**
     * Place the list below the line of the typed name, starting at the name.
     * They move to stay on the screen, and above the line if there is no room below it.
     * @return Where they are, relative to the text, or null if the name is scrolled out of view.
     */
    @Nullable
    private int[] placeCompletions(int x, int y, int[] lines) {
        String value = text.value();
        int line = 0;
        while (line < lines.length / 2 - 1 && completionsStart >= lines[line * 2 + 2]) {
            line++;
        }
        if (line < scroll || line >= scroll + visibleLines) {
            return null;
        }
        int lineBegin = lines[line * 2];
        int lineY = y + (line - scroll) * LINE_HEIGHT;
        int nameX = x + font.width(value.substring(lineBegin, Mth.clamp(completionsStart, lineBegin, lines[line * 2 + 1])));

        int rows = Math.min(COMPLETION_ROWS, completions.size());
        completionsFirst = Mth.clamp(completionIndex - rows / 2, 0, completions.size() - rows);
        int width = 0;
        for (Completion completion : completions) {
            width = Math.max(width, font.width(completion.name()) + 6 + font.width(completion.detail()));
        }
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        width = Math.min(width + 4, screenWidth - 4);
        int height = rows * LINE_HEIGHT + 2;
        int boxX = Math.max(2, Math.min(nameX - 2, screenWidth - 2 - width));
        int boxY = lineY + LINE_HEIGHT;
        if (boxY + height > screenHeight - 2) {
            boxY = lineY - 2 - height;
        }
        return new int[]{boxX - x, boxY - y, width, height};
    }

    /**
     * Draw the list shown while typing, or the signature of the call at the cursor, if the text was drawn since.
     * This is done after everything else, as slots and labels are drawn without depth, so they would cover them.
     * @param guiGraphics The graphics, without translation.
     */
    public void renderPopups(GuiGraphics guiGraphics) {
        if (!completionsPlaced) {
            completionsBox = null;
            signatureBox = null;
        }
        completionsPlaced = false;
        if (signatureBox != null) {
            renderSignature(guiGraphics);
        }
        if (completionsBox == null || getShownCompletions().isEmpty()) {
            return;
        }
        int boxX = textX + completionsBox[0];
        int boxY = textY + completionsBox[1];
        int width = completionsBox[2];
        int height = completionsBox[3];
        int rows = Math.min(COMPLETION_ROWS, completions.size());
        // Below tooltips
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);
        guiGraphics.fill(boxX - 1, boxY - 1, boxX + width + 1, boxY + height + 1, COLOR_POPUP_BORDER);
        guiGraphics.fill(boxX, boxY, boxX + width, boxY + height, COLOR_POPUP);
        for (int row = 0; row < rows; row++) {
            int index = completionsFirst + row;
            Completion completion = completions.get(index);
            int rowY = boxY + 1 + row * LINE_HEIGHT;
            if (index == completionIndex) {
                guiGraphics.fill(boxX, rowY, boxX + width, rowY + LINE_HEIGHT, COLOR_POPUP_SELECTED);
            }
            String name = font.plainSubstrByWidth(completion.name(), width - 4);
            guiGraphics.drawString(font, name, boxX + 2, rowY + 1, COLOR_CARET, false);
            int detailX = boxX + 2 + font.width(name) + 6;
            int detailWidth = boxX + width - 2 - detailX;
            FormattedText detail = completion.detail();
            if (font.width(detail) > detailWidth) {
                detail = FormattedText.composite(font.substrByWidth(detail, Math.max(0, detailWidth - font.width(OperatorLPElementRenderPattern.ELLIPSIS))),
                        FormattedText.of(OperatorLPElementRenderPattern.ELLIPSIS));
            }
            if (detailWidth > font.width(OperatorLPElementRenderPattern.ELLIPSIS)) {
                guiGraphics.drawString(font, Language.getInstance().getVisualOrder(detail), detailX, rowY + 1, COLOR_DETAIL, false);
            }
        }
        // How far down the list the shown entries are
        if (completions.size() > rows) {
            int barHeight = Math.max(3, (height - 2) * rows / completions.size());
            int barY = boxY + 1 + (height - 2 - barHeight) * completionsFirst / (completions.size() - rows);
            guiGraphics.fill(boxX + width - 1, barY, boxX + width, barY + barHeight, COLOR_SCROLLBAR);
        }
        guiGraphics.pose().popPose();
    }

    private void renderSignature(GuiGraphics guiGraphics) {
        int boxX = textX + signatureBox[0];
        int boxY = textY + signatureBox[1];
        int width = signatureBox[2];
        FormattedText shown = signature;
        if (font.width(shown) > width - 2) {
            shown = FormattedText.composite(font.substrByWidth(shown, Math.max(0, width - 2 - font.width(OperatorLPElementRenderPattern.ELLIPSIS))),
                    FormattedText.of(OperatorLPElementRenderPattern.ELLIPSIS));
        }
        // Below tooltips, in the colors of the list
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);
        int height = signatureBox[3];
        guiGraphics.fill(boxX - 1, boxY - 1, boxX + width + 1, boxY + height + 1, COLOR_POPUP_BORDER);
        guiGraphics.fill(boxX, boxY, boxX + width, boxY + height, COLOR_POPUP);
        guiGraphics.drawString(font, Language.getInstance().getVisualOrder(shown), boxX + 1, boxY + 1, COLOR_CARET, false);
        guiGraphics.pose().popPose();
    }

    /**
     * A method that can be called on the value before a point, or a type.
     * @param name The name to write.
     * @param detail The other inputs and output of a method, or the name of a type as the game shows it, in the colors of the types.
     * @param operator The operator of a method, or null for a type.
     */
    private static record Completion(String name, Component detail, @Nullable IOperator operator) {}

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
