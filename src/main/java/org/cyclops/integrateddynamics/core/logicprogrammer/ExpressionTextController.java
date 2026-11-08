package org.cyclops.integrateddynamics.core.logicprogrammer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParseException;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParser;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTypeChecker;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * This handles the text manipulations in the expressions text box.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public abstract class ExpressionTextController {

    protected final ContainerLogicProgrammerBase container;
    // The types of the cards in the inventory, which typed references can use
    @Nullable
    private Int2ObjectMap<IValueType<?>> inventoryCardTypes = null;
    // The typed text as it was last read
    @Nullable
    private String parsedFor = null;
    @Nullable
    private Expression parsed = null;
    @Nullable
    private ExpressionTypeChecker.Result parsedResult = null;
    @Nullable
    private ExpressionParseException parseError = null;

    protected ExpressionTextController(ContainerLogicProgrammerBase container) {
        this.container = container;
    }

    /**
     * @param container The container.
     * @return The text, which is kept in the container, so it survives the panel being recreated.
     */
    public static ExpressionTextField getTextField(ContainerLogicProgrammerBase container) {
        return container.getExpressionTextField(() -> new ExpressionTextField(Minecraft.getInstance().font, ExpressionPanelRendering.TEXT_WIDTH));
    }

    public ExpressionTextField getTextField() {
        return getTextField(container);
    }

    /**
     * @return The expression that the text belongs to, or null if there is none.
     */
    @Nullable
    protected abstract ExpressionEditor getEditor();

    /**
     * Apply the typed text if it reads as an expression.
     * This can make another element active, which replaces the panel, so it is not done while rendering.
     */
    protected abstract void applyText();

    /**
     * Revert typed text that is not applied yet, or otherwise go back one change.
     */
    protected abstract void undo();

    protected abstract void redo();

    /**
     * Write the expression in its canonical form.
     */
    protected abstract void formatText();

    // --------------- Reading ---------------

    /**
     * @return If text was typed that is not applied yet.
     */
    public boolean isDraft() {
        return getTextField().isDraft();
    }

    /**
     * @return The expression in the typed text, or null if it does not read as one, as read for the current text.
     */
    @Nullable
    public Expression parseText() {
        String text = getTextField().getValue();
        if (!text.equals(parsedFor)) {
            parsedFor = text;
            try {
                parsed = ExpressionEditor.parse(text, getParserContext());
                parsedResult = ExpressionTypeChecker.check(parsed, this::getRefType);
                parseError = null;
            } catch (ExpressionParseException e) {
                parsed = null;
                parsedResult = null;
                parseError = e;
            }
        }
        return parsed;
    }

    /**
     * @return The types of the expression in the typed text, or null if it does not read as one.
     */
    @Nullable
    public ExpressionTypeChecker.Result getParsedResult() {
        parseText();
        return parsedResult;
    }

    /**
     * @return Why the typed text does not read as an expression, or null if it does.
     */
    @Nullable
    public ExpressionParseException getParseError() {
        parseText();
        return parseError;
    }

    public ExpressionParser.Context getParserContext() {
        return ExpressionEditor.getParserContext(ValueDeseralizationContext.ofClient(), this::getRefType, this::getLabelIds);
    }

    /**
     * @param expression An expression.
     * @return Its canonical form.
     */
    public String printExact(Expression expression) {
        return ExpressionPrinter.printExact(expression, ValueDeseralizationContext.ofClient(), this::getLabel, this::getLabelIds, this::getRefType);
    }

    @Nullable
    public IValueType<?> getRefType(int variableId) {
        ExpressionEditor editor = getEditor();
        IValueType<?> type = editor != null ? editor.getRefType(variableId) : null;
        if (type != null) {
            return type;
        }
        if (inventoryCardTypes == null) {
            inventoryCardTypes = OperatorLPElement.getInventoryCardTypes(Minecraft.getInstance().player);
        }
        return inventoryCardTypes.get(variableId);
    }

    public String getLabel(int variableId) {
        return LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(variableId);
    }

    public IntList getLabelIds(String label) {
        return LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getVariableIds(label);
    }

    /**
     * @return Why typed text that is not applied yet does not read as an expression, or null if there is none.
     */
    @Nullable
    public ExpressionParseException getDraftError() {
        return isDraft() ? getParseError() : null;
    }

    // --------------- Typing ---------------

    public void focusText() {
        ExpressionTextField field = getTextField();
        if (!field.isFocused()) {
            field.setFocused(true);
            // Cards may have been added to the inventory since
            inventoryCardTypes = null;
        }
    }

    /**
     * Apply typed text once typing pauses, or when the panel is hidden, where the text can not be seen.
     * Called each tick, outside of rendering.
     */
    public void tick() {
        ExpressionTextField field = getTextField();
        boolean panel = ContainerScreenLogicProgrammerBase.isExpressionPanelOpen();
        if (field.isDraft() && (!panel || field.getMillisSinceEdit() >= ExpressionPanelRendering.APPLY_DELAY_MS)) {
            applyText();
        }
        if (!panel) {
            if (field.isDraft()) {
                field.revert();
            }
            field.setFocused(false);
        }
    }

    /**
     * Apply typed text that is not applied yet, such as when the gui closes.
     */
    public void applyPendingText() {
        if (isDraft()) {
            applyText();
        }
    }

    /**
     * Stop typing, applying valid text and reverting other text, as for Escape.
     * @return If the text had focus or was typed.
     */
    public boolean cancel() {
        ExpressionTextField field = getTextField();
        if (field.closeCompletions()) {
            return true;
        }
        if (!field.isDraft() && !field.isFocused()) {
            return false;
        }
        // Valid text is kept, as it would be after a pause, so this does not depend on how fast it is pressed
        if (field.isDraft() && parseText() != null) {
            applyText();
        } else {
            field.revert();
        }
        field.setFocused(false);
        return true;
    }

    public void copyText() {
        Minecraft.getInstance().keyboardHandler.setClipboard(getTextField().getValue());
    }

    /**
     * Handle a typed character while the text has focus.
     * @return If it was handled.
     */
    public boolean charTyped(char typedChar) {
        ExpressionTextField field = getTextField();
        if (!field.isFocused()) {
            return false;
        }
        field.charTyped(typedChar);
        return true;
    }

    /**
     * Handle a key while the text has focus.
     * @return If it was handled, which is any key, so keys such as the inventory key do not close the gui while typing.
     */
    public boolean keyPressed(int keyCode) {
        ExpressionTextField field = getTextField();
        if (!field.isFocused()) {
            return false;
        }
        if (field.keyPressedCompletion(keyCode)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            // A new line, as the text is applied once typing pauses, or with Escape
            field.insert("\n");
        } else if (isUndoKey(keyCode)) {
            undo();
        } else if (isRedoKey(keyCode)) {
            redo();
        } else {
            field.keyPressed(keyCode);
        }
        return true;
    }

    public static boolean isUndoKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_Z && Screen.hasControlDown() && !Screen.hasShiftDown();
    }

    public static boolean isRedoKey(int keyCode) {
        return Screen.hasControlDown() && (keyCode == GLFW.GLFW_KEY_Y || (keyCode == GLFW.GLFW_KEY_Z && Screen.hasShiftDown()));
    }

}
