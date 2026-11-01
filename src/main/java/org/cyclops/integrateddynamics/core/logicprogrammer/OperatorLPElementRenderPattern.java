package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Container;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetArrowedListField;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionParameter;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParseException;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParser;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTypeChecker;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.OperatorNames;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerExpressionActionPacket;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.List;

/**
 * Render pattern for operator elements, which supports editing nested expressions.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class OperatorLPElementRenderPattern extends RenderPattern<OperatorLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
        implements IRenderPatternValueTypeTooltip {

    static final int COLOR_TEXT = 0xFF404040;
    static final int COLOR_TEXT_LIGHT = 0xFFE0E0E0;
    private static final int COLOR_ERROR = 0xFFE03030;
    private static final int COLOR_SELECTED = 0xFF3070FF;
    private static final int COLOR_PARAM = 0xFFE8C840;
    private static final int COLOR_CONST = 0xFFB0D0F0;
    private static final int COLOR_BADGE = 0xFFFF9020;
    private static final int COLOR_PARAM_ARMED = 0xFFFFF0A0;
    private static final int COLOR_TAB = 0xFFD8DCE0;
    private static final int COLOR_LINK = 0xFF2050A0;
    static final int COLOR_STRIP = 0xFF373737;
    static final int COLOR_STRIP_LINE = 0xFF555555;
    private static final int COLOR_STRIP_HOVER = 0xFF5A5A5A;
    private static final int DOUBLE_CLICK_MS = 350;

    // Side panel layout, relative to the gui, with the same margin left and right, as the main texture covers the left of the panel
    static final int PANEL_X = ContainerLogicProgrammerBase.EXPRESSION_PANEL_X + 7;
    static final int PANEL_WIDTH = ContainerLogicProgrammerBase.EXPRESSION_PANEL_WIDTH - 13;
    static final int TITLE_Y = ContainerLogicProgrammerBase.EXPRESSION_PANEL_Y + 6;
    // Parameter chips wrap onto as many rows as needed, followed by the button to add one, the expression follows below them
    static final int PARAMS_Y = TITLE_Y + 12;
    static final int CHIP_HEIGHT = 12;
    // As many as 8 parameters with the longest names take, so the text keeps its room
    static final int PARAM_ROWS = 4;
    // In the tooltip of the parameters that do not fit
    static final int MAX_LISTED_PARAMETERS = 32;
    private static final int BREADCRUMB_SEPARATOR = 8;
    static final int LINE_HEIGHT = 9;
    // Buttons right of the title, with their icons in line with it
    static final int BUTTONS_Y = TITLE_Y - 2;
    static final int CONTENT_BOTTOM = ContainerLogicProgrammerBase.EXPRESSION_PANEL_Y + ContainerLogicProgrammerBase.EXPRESSION_PANEL_HEIGHT - 6;
    // The type, or an error instead, below the expression
    static final int FOOTER_LINES = 3;
    static final int FOOTER_HEIGHT = FOOTER_LINES * LINE_HEIGHT + 3;
    static final int FOOTER_Y = CONTENT_BOTTOM - FOOTER_HEIGHT;
    static final int TEXT_WIDTH = PANEL_WIDTH - 7;
    static final int COLOR_EMPTY = 0xFFA0A0A0;
    private static final int COLOR_EDITED = 0xFFFFFF60;
    static final int COLOR_TEXT_ERROR = 0xFFFF6060;
    static final int COLOR_FOCUS = 0xFFA0A0A0;
    static final String ELLIPSIS = "\u2026";
    // How long typing pauses before valid text is applied
    static final int APPLY_DELAY_MS = 750;

    // Editor layout, relative to the gui
    private static final int EDITOR_X = ContainerLogicProgrammerBase.BASE_X + 2;
    private static final int EDITOR_Y = ContainerLogicProgrammerBase.BASE_Y + ContainerLogicProgrammerBase.MAX_HEIGHT - 18;
    // As high as the type selector, which is the one of the list element
    private static final int EDITOR_HEIGHT = 15;
    private static final int EDITOR_RIGHT = ContainerLogicProgrammerBase.BASE_X + ContainerLogicProgrammerBase.MAX_WIDTH - 2;
    private static final int EDITOR_MIN_WIDTH = 60;
    // The arrow buttons of the type selector and the inset of its text
    private static final int TYPE_SELECTOR_PADDING = 28;

    private boolean renderTooltip = true;
    private final WidgetTextFieldExtended valueField;
    @Nullable
    private WidgetArrowedListField<IValueType<?>> typeSelector = null;
    private int guiLeft;
    private int guiTop;
    private long lastClickTime = 0;
    private int lastClickSlot = -1;
    @Nullable
    private Component editorError = null;

    // Clickable regions, relative to the gui, recalculated while rendering
    private final List<Region> regions = new ArrayList<>();
    // Undo, Redo, Copy and adding a parameter
    @Nullable
    private ExpressionPanelButtons buttons = null;
    // A parameter chip can also be dragged onto a slot, instead of clicking it and then the slot
    private int pressedParameter = -1;
    private double pressX;
    private double pressY;
    private boolean draggingParameter = false;

    // Editing the expression as text, in the box that shows it
    @Nullable
    private Int2ObjectMap<IValueType<?>> inventoryCardTypes = null;
    private int textBoxY = FOOTER_Y;
    @Nullable
    private String parsedFor = null;
    @Nullable
    private Expression parsedText = null;
    @Nullable
    private ExpressionTypeChecker.Result parsedTextResult = null;
    @Nullable
    private ExpressionParseException textError = null;

    public OperatorLPElementRenderPattern(OperatorLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                          ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.valueField = new WidgetTextFieldExtended(Minecraft.getInstance().font, 0, 0, getValueFieldWidth(Minecraft.getInstance().font), EDITOR_HEIGHT,
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_VALUE, ""), true) {
            // Drawn like the type selector next to it, without the vanilla box, but with the text inset as in that box
            @Override
            public boolean isBordered() {
                return false;
            }
        };
        this.valueField.setMaxLength(256);
        this.valueField.setBordered(true);
        this.valueField.setVisible(false);
        this.valueField.setTextColor(16777215);
        this.valueField.setCanLoseFocus(true);
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        // Their backgrounds and arrow buttons are drawn a pixel above and left of their position
        this.valueField.setX(guiLeft + EDITOR_X + 1);
        this.valueField.setY(guiTop + EDITOR_Y + 1);
        // The arrow buttons are placed when it is created
        int typeX = EDITOR_X + valueField.getWidth() + 3;
        this.typeSelector = new WidgetArrowedListField<>(Minecraft.getInstance().font, guiLeft + typeX, guiTop + EDITOR_Y + 1,
                EDITOR_RIGHT - typeX, EDITOR_HEIGHT, true, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_TYPE),
                true, ExpressionEditor.getParameterTypes());
        this.typeSelector.setListener(this::onTypeSelected);
        updateEditor();

        buttons = new ExpressionPanelButtons(() -> getEditor() != null && getEditor().getExpression().parameters().isEmpty(),
                this::addParameter, this::undo, this::redo, this::formatText, this::copyText);
        buttonList.addAll(buttons.getAll());
        updateButtons();
    }

    /**
     * @param font The font.
     * @return The width of the value field, which fills the row up to the type of a parameter, as wide as the widest type name.
     */
    protected static int getValueFieldWidth(Font font) {
        int nameWidth = 0;
        for (IValueType<?> type : ExpressionEditor.getParameterTypes()) {
            nameWidth = Math.max(nameWidth, font.width(Component.translatable(type.getTranslationKey())));
        }
        return Math.max(EDITOR_MIN_WIDTH, EDITOR_RIGHT - EDITOR_X - 3 - nameWidth - TYPE_SELECTOR_PADDING);
    }

    /**
     * Show the buttons that apply to the current state, before they are rendered or clicked.
     */
    protected void updateButtons() {
        ExpressionEditor editor = getEditor();
        boolean panel = editor != null && ContainerScreenLogicProgrammerBase.isExpressionPanelOpen();
        boolean draft = panel && isDraft();
        if (buttons == null) {
            return;
        }
        buttons.addParameter.visible = panel;
        // Undo also reverts typed text that is not applied yet
        buttons.undo.visible = panel;
        buttons.undo.active = draft || container.getExpressionHistory().canUndo();
        buttons.redo.visible = panel;
        buttons.redo.active = !draft && container.getExpressionHistory().canRedo();
        // Typed text is shown as typed, until it is formatted
        buttons.format.visible = panel;
        buttons.format.active = draft ? parseText() != null : panel && editor.getText() != null;
        buttons.copy.visible = panel;
        buttons.layout(guiLeft, guiTop);
        if (editor != null) {
            int[] button = layoutParameters(Minecraft.getInstance().font, editor.getExpression().parameters()).button();
            buttons.addParameter.setPosition(guiLeft + button[0], guiTop + button[1]);
        }
    }

    /**
     * Add a parameter, to the typed text while it is not applied yet, as applying it would undo the parameter otherwise.
     */
    protected void addParameter() {
        if (isDraft()) {
            focusText();
            getTextField().addParameter(getParserContext());
        } else {
            sendAction(ExpressionEditor.actionAddParam());
        }
    }

    /**
     * Lay out the parameter chips in rows below the title, followed by the button to add one.
     * The rows are limited, and a chip tells how many parameters do not fit, which can be used in the text.
     * @param font The font.
     * @param parameters The parameters.
     * @return Where the chips and the button go, relative to the gui.
     */
    protected ParameterChipLayout layoutParameters(Font font, List<ExpressionParameter> parameters) {
        // Long names are cut off, so they do not take a row each
        int[] widths = parameters.stream().mapToInt(parameter -> Math.min(font.width(parameter.name()) + 4, (PANEL_WIDTH - 2) / 2)).toArray();
        return ParameterChipLayout.layout(widths, hidden -> font.width(getMoreParametersText(hidden)) + 4, buttons.addParameter.getWidth(),
                PANEL_X, PARAMS_Y, PANEL_WIDTH, CHIP_HEIGHT + 1, PARAM_ROWS);
    }

    protected static Component getMoreParametersText(int hidden) {
        return Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_MORE, hidden);
    }

    @Override
    protected String getRenderSymbol() {
        return element.getCurrentOperator().getSymbol();
    }

    @Nullable
    protected ExpressionEditor getEditor() {
        return element.getEditor();
    }

    // --------------- Actions ---------------

    protected void sendAction(CompoundTag action) {
        container.applyExpressionAction(action);
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerExpressionActionPacket(action));
    }

    // --------------- Text ---------------

    /**
     * @return The text of the expression, which can be edited in place.
     */
    protected ExpressionTextField getTextField() {
        return getTextField(container);
    }

    /**
     * @param container The container.
     * @return The text of the expression, which is kept while the panel is recreated.
     */
    public static ExpressionTextField getTextField(ContainerLogicProgrammerBase container) {
        return container.getExpressionTextField(() -> new ExpressionTextField(Minecraft.getInstance().font, TEXT_WIDTH));
    }

    /**
     * @return If text was typed that is not applied yet. Slots, parameters and tabs wait until it is applied or reverted.
     */
    public boolean isDraft() {
        return getTextField().isDraft();
    }

    /**
     * @param expression The expression.
     * @param text The text it was typed as, or null.
     * @return The tokens of the text if it still reads as the expression, and otherwise of the expression in its usual form,
     *         such as after a label that the text uses changed.
     */
    protected List<ExpressionPrinter.Token> printTokens(Expression expression, @Nullable String text) {
        if (text != null) {
            try {
                ExpressionParser.Tokenized tokenized = ExpressionParser.tokenize(text, getParserContext());
                if (tokenized.expression().equals(expression)) {
                    return tokenized.tokens();
                }
            } catch (ExpressionParseException e) {
                // Printed instead
            }
        }
        return ExpressionPrinter.tokenizeExact(expression, ValueDeseralizationContext.ofClient(), this::getLabel, this::getLabelIds,
                ExpressionTypeChecker.check(expression, this::getRefType));
    }

    /**
     * @param expression An expression.
     * @return Its usual form.
     */
    protected String printExact(Expression expression) {
        return ExpressionPrinter.printExact(expression, ValueDeseralizationContext.ofClient(), this::getLabel, this::getLabelIds, this::getRefType);
    }

    /**
     * @return The expression in the draft, or null if it does not parse, as cached for the current text.
     */
    @Nullable
    protected Expression parseText() {
        String text = getTextField().getValue();
        if (!text.equals(parsedFor)) {
            parsedFor = text;
            try {
                parsedText = ExpressionEditor.parse(text, getParserContext());
                parsedTextResult = ExpressionTypeChecker.check(parsedText, this::getRefType);
                textError = null;
            } catch (ExpressionParseException e) {
                parsedText = null;
                parsedTextResult = null;
                textError = e;
            }
        }
        return parsedText;
    }

    protected ExpressionParser.Context getParserContext() {
        return ExpressionEditor.getParserContext(ValueDeseralizationContext.ofClient(), this::getRefType, this::getLabelIds);
    }

    protected void focusText() {
        ExpressionTextField field = getTextField();
        if (!field.isFocused()) {
            field.setFocused(true);
            inventoryCardTypes = OperatorLPElement.getInventoryCardTypes(Minecraft.getInstance().player);
        }
        selectSlot(-1);
        armParameter(-1);
        valueField.setFocused(false);
    }

    /**
     * Apply the typed text if it parses. It stays as typed, also on the card, unless it is the usual form.
     * The same expression typed in another way is applied too, as its text is kept.
     * This can make another element active, which replaces this panel, so it is not done while rendering.
     */
    protected void applyText() {
        Expression expression = parseText();
        ExpressionEditor editor = getEditor();
        if (expression == null || editor == null) {
            return;
        }
        String typed = getTextField().getValue();
        String text = typed.equals(printExact(expression)) ? null : typed;
        if (expression.equals(editor.getExpression()) && Objects.equals(text, editor.getText())) {
            getTextField().markApplied(null);
        } else {
            getTextField().markApplied(expression);
            sendAction(ExpressionEditor.actionSet(ValueDeseralizationContext.ofClient(), expression, text));
        }
    }

    /**
     * Show the expression in its usual form instead of as typed, applying typed text first if it parses.
     * This can make another element active, which replaces this panel.
     */
    protected void formatText() {
        ExpressionEditor editor = getEditor();
        Expression expression = isDraft() ? parseText() : editor == null ? null : editor.getExpression();
        if (expression == null) {
            return;
        }
        // The text of the expression replaces the typed text once it follows the expression
        getTextField().revert();
        sendAction(ExpressionEditor.actionSet(ValueDeseralizationContext.ofClient(), expression, null));
    }

    protected void revertText() {
        getTextField().revert();
    }

    /**
     * Apply typed text once typing pauses, or when the panel is hidden, and show the text of the expression in place of
     * applied text once it loses focus.
     * Called each tick, outside of rendering.
     */
    public void tickText() {
        ExpressionTextField field = getTextField();
        if (getEditor() == null) {
            return;
        }
        boolean panel = ContainerScreenLogicProgrammerBase.isExpressionPanelOpen();
        if (field.isDraft() && (!panel || field.getMillisSinceEdit() >= APPLY_DELAY_MS)) {
            applyText();
        }
        if (!panel) {
            // Slots should not wait on a draft that cannot be seen
            if (field.isDraft()) {
                field.revert();
            }
            field.setFocused(false);
        }
        if (!field.isFocused()) {
            field.showExpressionText();
        }
    }

    /**
     * Apply typed text that is not applied yet, such as when the gui closes.
     */
    public void applyPendingText() {
        if (getTextField().isDraft()) {
            applyText();
        }
    }

    /**
     * Revert typed text that is not applied yet, or otherwise go back one change.
     */
    protected void undo() {
        if (isDraft()) {
            revertText();
            return;
        }
        ExpressionHistory.Entry entry = container.getExpressionHistory().undo();
        if (entry != null) {
            setExpressionFromHistory(entry);
        }
    }

    protected void redo() {
        if (isDraft()) {
            return;
        }
        ExpressionHistory.Entry entry = container.getExpressionHistory().redo();
        if (entry != null) {
            setExpressionFromHistory(entry);
        }
    }

    /**
     * Set an expression from the history without recording it as a change.
     * This can make another element active, which replaces this panel.
     */
    protected void setExpressionFromHistory(ExpressionHistory.Entry entry) {
        sendAction(ExpressionEditor.actionSet(ValueDeseralizationContext.ofClient(), entry.expression(), entry.text()));
        if (container.getActiveElement() instanceof OperatorLPElement active && active.getEditor() != null) {
            container.getExpressionHistory().moved(new ExpressionHistory.Entry(active.getEditor().getExpression(), active.getEditor().getText()));
        }
    }

    protected void copyText() {
        Minecraft.getInstance().keyboardHandler.setClipboard(getTextField().getValue());
    }

    /**
     * Open the node of the character under the cursor in the slots, as when clicking a part of the expression.
     */
    protected void navigateToCursor() {
        ExpressionTextField field = getTextField();
        ExpressionEditor editor = getEditor();
        int index = field.getCharAtCursor();
        if (editor == null || !field.isPrinted() || index < 0) {
            return;
        }
        int[] path = getTokenPathAt(field.getTokens(), index);
        if (path != null) {
            int[] nodePath = ExpressionTrees.isOperatorPath(editor.getExpression().root(), path) ? path : ExpressionTrees.parent(path);
            if (!Arrays.equals(nodePath, editor.getEditPath())) {
                sendAction(ExpressionEditor.actionNavigate(nodePath));
            }
        }
    }

    @Nullable
    protected static int[] getTokenPathAt(List<ExpressionPrinter.Token> tokens, int index) {
        int position = 0;
        for (ExpressionPrinter.Token token : tokens) {
            position += token.text().length();
            if (index < position) {
                return token.path();
            }
        }
        return null;
    }

    @Nullable
    protected IValueType<?> getRefType(int variableId) {
        ExpressionEditor editor = getEditor();
        IValueType<?> type = editor != null ? editor.getRefType(variableId) : null;
        if (type != null) {
            return type;
        }
        // Also when the panel was recreated while typing, such as for another root operator
        if (inventoryCardTypes == null) {
            inventoryCardTypes = OperatorLPElement.getInventoryCardTypes(Minecraft.getInstance().player);
        }
        return inventoryCardTypes.get(variableId);
    }

    protected IntList getLabelIds(String label) {
        return LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getVariableIds(label);
    }

    protected boolean isInTextBox(double mouseX, double mouseY) {
        return ContainerScreenLogicProgrammerBase.isExpressionPanelOpen() && getEditor() != null
                && mouseX >= guiLeft + PANEL_X && mouseX < guiLeft + PANEL_X + PANEL_WIDTH
                && mouseY >= guiTop + textBoxY && mouseY < guiTop + FOOTER_Y;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (getTextField().mouseScrolledCompletion(mouseX - guiLeft - PANEL_X - 2, mouseY - guiTop - textBoxY - 2, scrollY)) {
            return true;
        }
        if (isInTextBox(mouseX, mouseY)) {
            getTextField().scroll(scrollY);
            return true;
        }
        return false;
    }

    protected void selectSlot(int slot) {
        element.setSelectedSlot(slot);
        container.setExpressionSlotFilter(slot >= 0 && getEditor() != null ? getEditor().getExpectedType(slot) : null);
        updateEditor();
    }

    protected void armParameter(int index) {
        element.setArmedParameter(index);
        updateEditor();
    }

    /**
     * Cancel the selected slot or armed parameter.
     * @return If something was selected.
     */
    public boolean cancelSelection() {
        ExpressionTextField field = getTextField();
        if (field.closeCompletions()) {
            return true;
        }
        if (field.isDraft() || field.isFocused()) {
            // Valid text is kept, as it would be after a pause, so this does not depend on how fast it is pressed
            if (field.isDraft() && parseText() != null) {
                applyText();
            } else {
                field.revert();
            }
            field.setFocused(false);
            return true;
        }
        if (element.getSelectedSlot() >= 0 || element.getArmedParameter() >= 0) {
            selectSlot(-1);
            armParameter(-1);
            return true;
        }
        return false;
    }

    /**
     * Update the value field for the current selection.
     */
    protected void updateEditor() {
        ExpressionEditor editor = getEditor();
        editorError = null;
        if (editor == null) {
            valueField.setVisible(false);
            return;
        }
        int armed = element.getArmedParameter();
        int slot = element.getSelectedSlot();
        if (armed >= 0 && armed < editor.getExpression().parameters().size()) {
            valueField.setVisible(true);
            valueField.setValue(editor.getExpression().parameters().get(armed).name());
            valueField.setFocused(true);
        } else if (slot >= 0 && slot < editor.getSlotCount() && ExpressionEditor.isTextEditable(editor.getExpectedType(slot))) {
            valueField.setVisible(true);
            ExpressionNode child = editor.getChild(slot);
            valueField.setValue(child instanceof ExpressionNode.Const constant && ExpressionEditor.isTextEditable(constant.value().getType())
                    ? ValueHelpers.toString(constant.value()) : "");
            valueField.setFocused(true);
        } else {
            valueField.setVisible(false);
            valueField.setFocused(false);
        }
    }

    protected void submitEditor() {
        ExpressionEditor editor = getEditor();
        if (editor == null) {
            return;
        }
        int armed = element.getArmedParameter();
        int slot = element.getSelectedSlot();
        if (armed >= 0) {
            sendAction(ExpressionEditor.actionRenameParam(armed, valueField.getValue()));
            armParameter(-1);
        } else if (slot >= 0) {
            IValueType<?> expectedType = editor.getExpectedType(slot);
            IValue value = ExpressionEditor.parseValue(expectedType, valueField.getValue());
            if (value == null) {
                editorError = Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_VALUE_INVALID,
                        Component.translatable(expectedType.getTranslationKey()));
                return;
            }
            sendAction(ExpressionEditor.actionConst(ValueDeseralizationContext.ofClient(), editor.getEditPath(), slot, value));
            selectSlot(-1);
        }
    }

    /**
     * Called by the gui when an operator was picked from the list while a slot is selected.
     * @param operator The operator.
     * @return If the operator was nested.
     */
    public boolean onOperatorPicked(IOperator operator) {
        ExpressionEditor editor = getEditor();
        int slot = element.getSelectedSlot();
        if (editor == null || slot < 0 || isDraft()) {
            return false;
        }
        if (ExpressionEditor.isPickedAsValue(editor.getExpectedType(slot), operator, Screen.hasShiftDown())) {
            sendAction(ExpressionEditor.actionConst(ValueDeseralizationContext.ofClient(), editor.getEditPath(), slot,
                    ValueTypeOperator.ValueOperator.of(operator)));
            selectSlot(-1);
            return true;
        }
        sendAction(ExpressionEditor.actionOperator(editor.getEditPath(), slot, operator));
        selectSlot(-1);
        // Open the nested operator right away, as its inputs are usually filled in next
        if (operator.getRequiredInputLength() > 0) {
            sendAction(ExpressionEditor.actionNavigate(ExpressionTrees.append(editor.getEditPath(), slot)));
        }
        return true;
    }

    // --------------- Input ---------------

    protected int getSlotAt(double mouseX, double mouseY) {
        IConfigRenderPattern pattern = element.getRenderPattern();
        Pair<Integer, Integer>[] slotPositions = pattern.getSlotPositions();
        for (int i = 0; i < slotPositions.length; i++) {
            int x = guiLeft + getX() + slotPositions[i].getLeft();
            int y = guiTop + getY() + slotPositions[i].getRight();
            if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        ExpressionEditor editor = getEditor();
        if (editor == null) {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        // A release can get lost, such as outside the window
        pressedParameter = -1;
        draggingParameter = false;
        getTextField().setPressed(false);
        // The list shown while typing, which can cover the rest of the panel
        if (getTextField().mouseClickedCompletion(mouseX - guiLeft - PANEL_X - 2, mouseY - guiTop - textBoxY - 2)) {
            return true;
        }
        for (Button button : Lists.newArrayList(buttonList)) {
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }

        // The text of the expression, where clicking a part also opens it in the slots
        ExpressionTextField field = getTextField();
        if (isInTextBox(mouseX, mouseY)) {
            focusText();
            field.mouseClicked(mouseX - guiLeft - PANEL_X - 2, mouseY - guiTop - textBoxY - 2);
            field.setPressed(true);
            navigateToCursor();
            return true;
        }
        field.setFocused(false);
        // Slots, parameters and tabs would be replaced when applying the text, so valid text is applied first
        if (field.isDraft()) {
            if (parseText() != null) {
                applyText();
                return true;
            }
            return false;
        }

        if (isTypeSelectorShown() && typeSelector.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        if (valueField.isVisible()) {
            if (valueField.mouseClicked(mouseX, mouseY, mouseButton)) {
                valueField.setFocused(true);
                return true;
            }
            valueField.setFocused(false);
        }

        // Clickable regions (breadcrumb, parameters, strip)
        pressX = mouseX;
        pressY = mouseY;
        for (Region region : Lists.newArrayList(regions)) {
            if (region.contains(mouseX - guiLeft, mouseY - guiTop)) {
                region.action().onClick(mouseButton);
                return true;
            }
        }

        int slot = getSlotAt(mouseX, mouseY);
        if (slot >= 0 && slot < editor.getSlotCount() && container.getCarried().isEmpty()) {
            ExpressionNode child = editor.getChild(slot);
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                sendAction(ExpressionEditor.actionClear(editor.getEditPath(), slot));
                selectSlot(-1);
                return true;
            }
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                long now = Util.getMillis();
                boolean doubleClick = lastClickSlot == slot && now - lastClickTime < DOUBLE_CLICK_MS;
                lastClickTime = now;
                lastClickSlot = slot;
                int armed = element.getArmedParameter();
                if (armed >= 0) {
                    sendAction(ExpressionEditor.actionParam(editor.getEditPath(), slot, armed));
                    armParameter(-1);
                    selectSlot(-1);
                } else if (child instanceof ExpressionNode.Operator && (doubleClick || element.getSelectedSlot() == slot)) {
                    selectSlot(-1);
                    sendAction(ExpressionEditor.actionNavigate(ExpressionTrees.append(editor.getEditPath(), slot)));
                } else {
                    selectSlot(element.getSelectedSlot() == slot ? -1 : slot);
                }
                return true;
            }
        }

        // Clicking the empty config area cancels the current selection
        if ((element.getSelectedSlot() >= 0 || element.getArmedParameter() >= 0)
                && gui.isHovering(ContainerLogicProgrammerBase.BASE_X, ContainerLogicProgrammerBase.BASE_Y,
                ContainerLogicProgrammerBase.MAX_WIDTH, ContainerLogicProgrammerBase.MAX_HEIGHT, mouseX, mouseY)) {
            selectSlot(-1);
            armParameter(-1);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (getTextField().isPressed()) {
            getTextField().mouseDragged(mouseX - guiLeft - PANEL_X - 2, mouseY - guiTop - textBoxY - 2);
            return true;
        }
        if (pressedParameter < 0 || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (!draggingParameter && Math.abs(mouseX - pressX) + Math.abs(mouseY - pressY) > 3) {
            draggingParameter = true;
        }
        return true;
    }

    /**
     * Drop a dragged parameter into the slot under the mouse, or cancel when it is not over a slot.
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (getTextField().isPressed()) {
            getTextField().setPressed(false);
            return true;
        }
        int index = pressedParameter;
        boolean dragged = draggingParameter;
        pressedParameter = -1;
        draggingParameter = false;
        ExpressionEditor editor = getEditor();
        if (!dragged || editor == null) {
            return false;
        }
        int slot = getSlotAt(mouseX, mouseY);
        if (slot >= 0 && slot < editor.getSlotCount() && index < editor.getExpression().parameters().size()) {
            sendAction(ExpressionEditor.actionParam(editor.getEditPath(), slot, index));
            selectSlot(-1);
        }
        armParameter(-1);
        return true;
    }

    // In brackets for an operator, as a nested operator shows the same name
    private static String getConstantText(IValue value) {
        String text = ExpressionPrinter.printValue(value);
        return value instanceof ValueTypeOperator.ValueOperator && !text.startsWith("(") ? "(" + text + ")" : text;
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        ExpressionTextField field = getTextField();
        if (field.isFocused()) {
            field.charTyped(typedChar);
            return true;
        }
        if (valueField.isVisible() && valueField.isFocused()) {
            valueField.charTyped(typedChar, keyCode);
            editorError = null;
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        ExpressionTextField field = getTextField();
        boolean undoKey = typedChar == GLFW.GLFW_KEY_Z && Screen.hasControlDown() && !Screen.hasShiftDown();
        boolean redoKey = Screen.hasControlDown() && (typedChar == GLFW.GLFW_KEY_Y || (typedChar == GLFW.GLFW_KEY_Z && Screen.hasShiftDown()));
        if (field.isFocused()) {
            if (field.keyPressedCompletion(typedChar)) {
                return true;
            }
            if (typedChar == GLFW.GLFW_KEY_ENTER || typedChar == GLFW.GLFW_KEY_KP_ENTER) {
                // A new line, as the text is applied once typing pauses, or with Escape
                field.insert("\n");
            } else if (undoKey) {
                undo();
            } else if (redoKey) {
                redo();
            } else {
                field.keyPressed(typedChar);
            }
            // Keep keys such as the inventory key from closing the gui while typing
            return true;
        }
        if (valueField.isVisible() && valueField.isFocused()) {
            if (typedChar == GLFW.GLFW_KEY_ENTER || typedChar == GLFW.GLFW_KEY_KP_ENTER) {
                submitEditor();
                return true;
            }
            valueField.keyPressed(typedChar, keyCode, modifiers);
            editorError = null;
            return true;
        }
        if (undoKey || redoKey) {
            if (undoKey) {
                undo();
            } else {
                redo();
            }
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    // --------------- Rendering ---------------

    /**
     * Draw text centered in a box, at the largest of a few scales at which it fits, truncating it at the smallest one.
     */
    protected static void drawFitted(GuiGraphics guiGraphics, Font font, String text, int x, int y, int width, int height, int color) {
        float scale = 0.5F;
        for (float candidate : new float[]{1F, 0.75F}) {
            if (font.width(text) * candidate <= width) {
                scale = candidate;
                break;
            }
        }
        if (font.width(text) * scale > width) {
            while (text.length() > 1 && font.width(text + "\u2026") * scale > width) {
                text = text.substring(0, text.length() - 1);
            }
            text = text + "\u2026";
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + (width - font.width(text) * scale) / 2F, y + (height - 7 * scale) / 2F, 300);
        guiGraphics.pose().scale(scale, scale, 1F);
        guiGraphics.drawString(font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    protected static int getContrastingTextColor(int background) {
        int r = (background >> 16) & 0xFF;
        int g = (background >> 8) & 0xFF;
        int b = background & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000 > 128 ? COLOR_TEXT : COLOR_TEXT_LIGHT;
    }

    /**
     * Draw the outline of the text box while it is focused, and otherwise the line between it and the footer,
     * which is where the bottom of the outline goes.
     * @param bottom Where the footer starts.
     */
    static void renderTextBoxBorder(GuiGraphics guiGraphics, int x, int y, int bottom, boolean focused) {
        if (focused) {
            drawOutline(guiGraphics, x, y, PANEL_WIDTH, bottom - y, COLOR_FOCUS);
        } else {
            guiGraphics.fill(x, bottom - 1, x + PANEL_WIDTH, bottom, COLOR_STRIP_LINE);
        }
    }

    protected static void drawOutline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        guiGraphics.fill(x, y, x + 1, y + height, color);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    protected String getLabel(int variableId) {
        return LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(variableId);
    }

    @Override
    public void renderBg(GuiGraphics guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        updateButtons();
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        regions.clear();
        ExpressionEditor editor = getEditor();
        if (editor == null) {
            return;
        }
        ExpressionTypeChecker.Result result = editor.check();
        int[] editPath = editor.getEditPath();
        int dropSlot = draggingParameter ? getSlotAt(mouseX, mouseY) : -1;

        // Slot contents that are not backed by items
        Pair<Integer, Integer>[] slotPositions = element.getRenderPattern().getSlotPositions();
        for (int i = 0; i < slotPositions.length && i < editor.getSlotCount(); i++) {
            int x = guiLeft + getX() + slotPositions[i].getLeft();
            int y = guiTop + getY() + slotPositions[i].getRight();
            ExpressionNode child = editor.getChild(i);
            switch (child) {
                case ExpressionNode.Operator op -> {
                    int background = 0xFF000000 | op.operator().getOutputType().getDisplayColor();
                    guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, background);
                    drawFitted(guiGraphics, fontRenderer, OperatorNames.getNodeName(op, ExpressionTrees.append(editPath, i), result), x + 1, y + 1, 16, 16,
                            getContrastingTextColor(background));
                    if (ExpressionTrees.countEmpty(op) > 0) {
                        // Badge for unfilled inputs
                        guiGraphics.fill(x + 12, y + 1, x + 17, y + 6, 0xFF000000);
                        guiGraphics.fill(x + 13, y + 2, x + 16, y + 5, COLOR_BADGE);
                    }
                }
                case ExpressionNode.Const constant -> {
                    if (OperatorLPElement.getConstantItem(constant).isEmpty()) {
                        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_CONST);
                        drawFitted(guiGraphics, fontRenderer, getConstantText(constant.value()), x + 1, y + 1, 16, 16, COLOR_TEXT);
                    }
                }
                case ExpressionNode.Param param -> {
                    guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_PARAM);
                    ExpressionParameter parameter = editor.getExpression().parameters().get(param.index());
                    drawFitted(guiGraphics, fontRenderer, parameter.name(), x + 1, y + 1, 16, 16, COLOR_TEXT);
                }
                default -> {}
            }
            if (result.getError(editPath, i) != null || result.hasErrorWithin(ExpressionTrees.append(editPath, i))) {
                drawOutline(guiGraphics, x, y, 18, 18, COLOR_ERROR);
            } else if (element.getSelectedSlot() == i || dropSlot == i) {
                drawOutline(guiGraphics, x, y, 18, 18, COLOR_SELECTED);
            }
        }

        // Breadcrumb, as tabs for the operators from the root to the edited one
        if (editPath.length > 0) {
            int x = ContainerLogicProgrammerBase.BASE_X + 2;
            int y = ContainerLogicProgrammerBase.BASE_Y + 2;
            List<String> names = new ArrayList<>();
            for (int i = 0; i < editor.getOperatorsAlongPath(editPath).size(); i++) {
                int[] tabPath = Arrays.copyOf(editPath, i);
                names.add(OperatorNames.getNodeName((ExpressionNode.Operator) ExpressionTrees.get(editor.getExpression().root(), tabPath), tabPath, result));
            }
            List<Tab> tabs = layoutBreadcrumb(fontRenderer, names, ContainerLogicProgrammerBase.MAX_WIDTH - 4);
            for (int i = 0; i < tabs.size(); i++) {
                Tab tab = tabs.get(i);
                if (i > 0) {
                    guiGraphics.drawString(fontRenderer, "\u203a", guiLeft + x + 1, guiTop + y + 2, COLOR_TEXT, false);
                    x += BREADCRUMB_SEPARATOR;
                }
                boolean current = i == tabs.size() - 1;
                guiGraphics.fill(guiLeft + x, guiTop + y, guiLeft + x + tab.width(), guiTop + y + 12, current ? 0xFFFFFFFF : COLOR_TAB);
                drawOutline(guiGraphics, guiLeft + x, guiTop + y, tab.width(), 12, current ? COLOR_TEXT : 0xFF8B8B8B);
                guiGraphics.drawString(fontRenderer, tab.text(), guiLeft + x + 3, guiTop + y + 2, current ? COLOR_TEXT : COLOR_LINK, false);
                if (!current) {
                    int[] tabPath = Arrays.copyOf(editPath, tab.depth());
                    // Collapsed tabs tell which operators they stand for
                    Component tooltip = tab.collapsed()
                            ? Component.literal(String.join(" \u203a ", names.subList(1, tab.depth() + 1)))
                            : Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_BREADCRUMB);
                    regions.add(new Region(x, y, tab.width(), 12, button -> {
                        selectSlot(-1);
                        sendAction(ExpressionEditor.actionNavigate(tabPath));
                    }, tooltip));
                }
                x += tab.width();
            }
        }

        // Value editor
        if (valueField.isVisible()) {
            valueField.render(guiGraphics, mouseX, mouseY, partialTicks);
            int x = EDITOR_X + valueField.getWidth() + 3;
            // In line with the text in the field
            int y = EDITOR_Y + 1 + (EDITOR_HEIGHT - 8) / 2;
            if (isTypeSelectorShown()) {
                renderTypeSelector(guiGraphics, editor, mouseX, mouseY, partialTicks);
            } else {
                Component hint = editorError != null ? editorError : getEditorHint(editor);
                int available = EDITOR_RIGHT - x;
                float scale = Math.min(1F, (float) available / fontRenderer.width(hint));
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(guiLeft + x, guiTop + y + (1 - scale) * 4, 300);
                guiGraphics.pose().scale(scale, scale, 1F);
                guiGraphics.drawString(fontRenderer, hint, 0, 0, editorError != null ? COLOR_ERROR : COLOR_TEXT, false);
                guiGraphics.pose().popPose();
            }
        }

        // Changes from slots, parameters and text alike can be undone
        container.getExpressionHistory().observe(editor.getExpression(), editor.getText());
        ExpressionTextField field = getTextField();
        field.sync(editor.getExpression(), editor.getText(), () -> printTokens(editor.getExpression(), editor.getText()));
        if (ContainerScreenLogicProgrammerBase.isExpressionPanelOpen()) {
            renderTitle(guiGraphics, guiLeft, guiTop, fontRenderer);
            int hoveredSlot = getSlotAt(mouseX, mouseY);
            int[] hoveredPath = hoveredSlot >= 0 && hoveredSlot < editor.getSlotCount() ? ExpressionTrees.append(editPath, hoveredSlot) : null;
            textBoxY = renderParameters(guiGraphics, guiLeft, guiTop, fontRenderer, editor);
            renderTextField(guiGraphics, guiLeft, guiTop, editor, result, hoveredPath, mouseX, mouseY);
            // The signature of the call at the cursor is shown above its line instead, so this always describes the expression
            if (!field.isDraft()) {
                renderStatus(guiGraphics, guiLeft, guiTop, fontRenderer, editor.getExpression(), result, regions);
            } else if (parseText() != null) {
                renderStatus(guiGraphics, guiLeft, guiTop, fontRenderer, parsedText, parsedTextResult, regions);
            } else {
                renderTextError(guiGraphics, guiLeft, guiTop, fontRenderer, textError, regions);
            }
        }
    }

    /**
     * Lay out the breadcrumb in the given width, as paths are shortened:
     * the tabs in the middle that do not fit are collapsed into one, and the names of the others are cut off if they still do not fit.
     * @param font The font.
     * @param names The names of the operators from the root to the edited one.
     * @param available The width.
     * @return The tabs to draw, where a collapsed one leads to the last operator it stands for.
     */
    protected static List<Tab> layoutBreadcrumb(Font font, List<String> names, int available) {
        int count = names.size();
        // Show the root, then the last tabs from this one on, with a collapsed tab in between if any are left out
        int shownFrom = 1;
        while (shownFrom < count - 1 && getBreadcrumbWidth(font, names, shownFrom, Integer.MAX_VALUE) > available) {
            shownFrom++;
        }
        int maxWidth = names.stream().mapToInt(font::width).max().orElse(0);
        while (maxWidth > font.width(ELLIPSIS) && getBreadcrumbWidth(font, names, shownFrom, maxWidth) > available) {
            maxWidth--;
        }
        List<Tab> tabs = new ArrayList<>();
        tabs.add(new Tab(font, cutOff(font, names.get(0), maxWidth), 0, false));
        if (shownFrom > 1) {
            tabs.add(new Tab(font, ELLIPSIS, shownFrom - 1, true));
        }
        for (int i = shownFrom; i < count; i++) {
            tabs.add(new Tab(font, cutOff(font, names.get(i), maxWidth), i, false));
        }
        return tabs;
    }

    private static int getBreadcrumbWidth(Font font, List<String> names, int shownFrom, int maxWidth) {
        int width = Math.min(font.width(names.get(0)), maxWidth) + 6;
        if (shownFrom > 1) {
            width += BREADCRUMB_SEPARATOR + font.width(ELLIPSIS) + 6;
        }
        for (int i = shownFrom; i < names.size(); i++) {
            width += BREADCRUMB_SEPARATOR + Math.min(font.width(names.get(i)), maxWidth) + 6;
        }
        return width;
    }

    private static String cutOff(Font font, String text, int width) {
        return font.width(text) <= width ? text : font.plainSubstrByWidth(text, width - font.width(ELLIPSIS)) + ELLIPSIS;
    }

    /**
     * A tab of the breadcrumb.
     * @param text Its text.
     * @param depth The depth of the operator it leads to.
     * @param collapsed If it stands for several operators that are left out.
     * @param width Its width.
     */
    protected static record Tab(String text, int depth, boolean collapsed, int width) {
        Tab(Font font, String text, int depth, boolean collapsed) {
            this(text, depth, collapsed, font.width(text) + 6);
        }
    }

    /**
     * @return If the type of the selected parameter is shown right of its name.
     */
    protected boolean isTypeSelectorShown() {
        ExpressionEditor editor = getEditor();
        int armed = element.getArmedParameter();
        return typeSelector != null && valueField.isVisible() && editor != null && armed >= 0 && armed < editor.getExpression().parameters().size();
    }

    protected void onTypeSelected() {
        IValueType<?> type = typeSelector.getActiveElement();
        if (isTypeSelectorShown() && type != null) {
            sendAction(ExpressionEditor.actionParamType(element.getArmedParameter(), type));
        }
    }

    protected void renderTypeSelector(GuiGraphics guiGraphics, ExpressionEditor editor, int mouseX, int mouseY, float partialTicks) {
        // Follow the type, which also changes when the parameter is first dropped in a slot
        IValueType<?> type = editor.getExpression().parameters().get(element.getArmedParameter()).type();
        if (typeSelector.getActiveElement() != type) {
            typeSelector.setListener(null);
            typeSelector.setActiveElement(type);
            typeSelector.setListener(this::onTypeSelected);
        }
        typeSelector.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    protected Component getEditorHint(ExpressionEditor editor) {
        IValueType<?> expectedType = editor.getExpectedType(element.getSelectedSlot());
        return Component.translatable(expectedType.getTranslationKey());
    }

    static void renderTitle(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font) {
        guiGraphics.drawString(font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TITLE),
                guiLeft + PANEL_X, guiTop + TITLE_Y, COLOR_TEXT, false);
    }

    /**
     * Draw the message of an error in the text in the footer, without where it is, as the text marks it.
     */
    static void renderTextError(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, ExpressionParseException error,
                                List<Region> regions) {
        renderFooter(guiGraphics, guiLeft, guiTop, font, error.getComponent().copy().withStyle(ChatFormatting.RED), null, regions);
    }

    /**
     * Draw the parameters as chips, which can be dragged onto slots.
     * Only names, so all parameters fit, their types are in the tooltip and in the type of the expression.
     * @return The y position below the parameters and the button to add one, relative to the gui.
     */
    protected int renderParameters(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor) {
        List<ExpressionParameter> parameters = editor.getExpression().parameters();
        ParameterChipLayout layout = layoutParameters(font, parameters);
        List<int[]> chips = layout.chips();
        for (int i = 0; i < chips.size(); i++) {
            ExpressionParameter parameter = parameters.get(i);
            int x = chips.get(i)[0];
            int y = chips.get(i)[1];
            int width = chips.get(i)[2];
            String name = parameter.name();
            if (width < font.width(name) + 4) {
                name = font.plainSubstrByWidth(name, width - 4 - font.width(ELLIPSIS)) + ELLIPSIS;
            }
            boolean armed = element.getArmedParameter() == i;
            guiGraphics.fill(guiLeft + x, guiTop + y, guiLeft + x + width, guiTop + y + CHIP_HEIGHT, armed ? COLOR_PARAM_ARMED : COLOR_PARAM);
            drawOutline(guiGraphics, guiLeft + x, guiTop + y, width, CHIP_HEIGHT, armed ? COLOR_SELECTED : 0xFF806010);
            guiGraphics.drawString(font, name, guiLeft + x + 2, guiTop + y + 2, COLOR_TEXT, false);
            int index = i;
            regions.add(new Region(x, y, width, CHIP_HEIGHT, button -> {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    armParameter(-1);
                    sendAction(ExpressionEditor.actionRemoveParam(index));
                } else {
                    armParameter(element.getArmedParameter() == index ? -1 : index);
                    selectSlot(-1);
                    pressedParameter = index;
                }
            }, getParameterTooltip(parameter)));
        }
        if (layout.more() != null) {
            renderMoreParameters(guiGraphics, guiLeft, guiTop, font, layout.more(), parameters.subList(chips.size(), parameters.size()));
        }
        return layout.button()[1] + CHIP_HEIGHT + 2;
    }

    protected static Component getParameterTooltip(ExpressionParameter parameter) {
        return Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_TOOLTIP, parameter.name(),
                Component.translatable(parameter.type().getTranslationKey()).withStyle(parameter.type().getDisplayColorFormat()));
    }

    /**
     * Draw the chip of the parameters that do not fit, with their names and types in its tooltip.
     */
    protected void renderMoreParameters(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, int[] chip,
                                        List<ExpressionParameter> hidden) {
        guiGraphics.fill(guiLeft + chip[0], guiTop + chip[1], guiLeft + chip[0] + chip[2], guiTop + chip[1] + CHIP_HEIGHT, COLOR_STRIP);
        drawOutline(guiGraphics, guiLeft + chip[0], guiTop + chip[1], chip[2], CHIP_HEIGHT, 0xFF806010);
        guiGraphics.drawString(font, getMoreParametersText(hidden.size()), guiLeft + chip[0] + 2, guiTop + chip[1] + 2, COLOR_TEXT_LIGHT, false);
        MutableComponent names = Component.empty();
        for (int i = 0; i < Math.min(hidden.size(), MAX_LISTED_PARAMETERS); i++) {
            names.append(i > 0 ? Component.literal(", ") : Component.empty()).append(getParameterTooltip(hidden.get(i)));
        }
        if (hidden.size() > MAX_LISTED_PARAMETERS) {
            names.append(", " + ELLIPSIS);
        }
        regions.add(new Region(chip[0], chip[1], chip[2], CHIP_HEIGHT, button -> {},
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_MORE_TOOLTIP, names)));
    }

    protected static boolean startsWith(int[] path, int[] prefix) {
        return path.length >= prefix.length && Arrays.equals(Arrays.copyOf(path, prefix.length), prefix);
    }

    /**
     * Draw the text of the expression, which can be edited in place.
     * It is colored like the slots: the edited operator, errors, empty slots, and the part of a hovered slot.
     */
    protected void renderTextField(GuiGraphics guiGraphics, int guiLeft, int guiTop, ExpressionEditor editor,
                                   ExpressionTypeChecker.Result result, @Nullable int[] hoveredPath, int mouseX, int mouseY) {
        ExpressionTextField field = getTextField();
        field.setParserContext(this::getParserContext);
        int x = guiLeft + PANEL_X;
        int y = guiTop + textBoxY;
        guiGraphics.fill(x, y, x + PANEL_WIDTH, guiTop + CONTENT_BOTTOM, COLOR_STRIP);
        renderTextBoxBorder(guiGraphics, x, y, guiTop + FOOTER_Y, field.isFocused());

        // Errors are only marked without a draft, as the footer shows those of the draft
        boolean draft = field.isDraft();
        String printed = field.getPrinted();
        int[] colors = new int[printed.length()];
        int[] backgrounds = new int[printed.length()];
        Arrays.fill(colors, COLOR_TEXT_LIGHT);
        Expression expression = editor.getExpression();
        int position = 0;
        for (ExpressionPrinter.Token token : field.getTokens()) {
            int length = token.text().length();
            int[] path = token.path();
            if (path != null && position + length <= printed.length()) {
                int[] nodePath = ExpressionTrees.isOperatorPath(expression.root(), path) ? path : ExpressionTrees.parent(path);
                int color = COLOR_TEXT_LIGHT;
                if (Arrays.equals(nodePath, editor.getEditPath())) {
                    color = COLOR_EDITED;
                }
                if (!draft && result.hasErrorAt(nodePath) && (path == nodePath || result.getError(nodePath, path[path.length - 1]) != null)) {
                    color = COLOR_TEXT_ERROR;
                }
                if (ExpressionTrees.get(expression.root(), path) instanceof ExpressionNode.Empty) {
                    color = COLOR_EMPTY;
                }
                Arrays.fill(colors, position, position + length, color);
                if (!draft && hoveredPath != null && startsWith(path, hoveredPath)) {
                    Arrays.fill(backgrounds, position, position + length, COLOR_STRIP_HOVER);
                }
            }
            position += length;
        }
        if (!field.isPrinted()) {
            // The text around what was typed keeps its colors
            colors = field.followDraft(colors, COLOR_TEXT_LIGHT);
            backgrounds = field.followDraft(backgrounds, 0);
        }
        int mark = -1;
        int markEnd = -1;
        if (draft && parseText() == null && textError != null) {
            mark = Math.min(textError.getPosition(), field.getValue().length());
            markEnd = Math.min(textError.getEnd(), field.getValue().length());
        }
        field.render(guiGraphics, x + 2, y + 2, PANEL_WIDTH - 3, guiTop + FOOTER_Y - y - 3, colors, backgrounds, mark, markEnd, COLOR_TEXT_ERROR);
        if (mark >= 0) {
            field.renderErrorTooltip(guiGraphics, mark, markEnd, textError.getComponent(), mouseX, mouseY);
        } else if (!draft) {
            // The type errors of the part of the text under the mouse, which is red
            int index = field.getCharAtMouse(mouseX, mouseY);
            int start = 0;
            for (ExpressionPrinter.Token token : field.getTokens()) {
                int end = start + token.text().length();
                if (index >= start && index < end) {
                    Component message = token.path() == null ? null : result.getErrorMessage(expression.root(), token.path());
                    if (message != null) {
                        field.renderPartTooltip(guiGraphics, start, end, message, mouseX, mouseY);
                    }
                    break;
                }
                start = end;
            }
        }
    }

    /**
     * Draw the type of the expression in the footer, in the colors of its types as in tooltips,
     * or the first error instead, which keeps it from being written.
     * The check mark next to the operator name already tells when it can be written.
     */
    static void renderStatus(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, Expression expression,
                             ExpressionTypeChecker.Result result, List<Region> regions) {
        if (!result.errors().isEmpty()) {
            ExpressionTypeChecker.Error error = result.errors().get(0);
            Component status = error.message();
            if (error.slot() >= 0) {
                String operatorName = ExpressionTrees.get(expression.root(), error.path()) instanceof ExpressionNode.Operator op
                        ? OperatorNames.getNodeName(op, error.path(), result) : "?";
                status = Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_STATUS_ERRORS_SLOT, result.errors().size(), error.slot() + 1, operatorName);
            }
            renderFooter(guiGraphics, guiLeft, guiTop, font, status.copy().withStyle(ChatFormatting.RED),
                    error.message().copy().withStyle(ChatFormatting.RED), regions);
            return;
        }
        IValueType<?> outputType = result.outputType();
        Component type = expression.hasParameters()
                ? ValueTypeOperator.getSignature(ExpressionTypeChecker.getInputTypes(expression), outputType)
                : Component.translatable(outputType.getTranslationKey()).withStyle(outputType.getDisplayColorFormat());
        renderFooter(guiGraphics, guiLeft, guiTop, font, type, null, regions);
    }

    /**
     * Draw text centered in the footer, on the dark background of the expression where the colors of types are readable.
     * Text that does not fit ends in an ellipsis.
     * @param tooltip Shown when hovering the footer, defaults to the text if it does not fit.
     * @param regions Where the region for the tooltip is added.
     */
    static void renderFooter(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, Component text, @Nullable Component tooltip,
                             List<Region> regions) {
        guiGraphics.fill(guiLeft + PANEL_X, guiTop + FOOTER_Y, guiLeft + PANEL_X + PANEL_WIDTH, guiTop + CONTENT_BOTTOM, COLOR_STRIP);
        int width = PANEL_WIDTH - 4;
        List<FormattedText> lines = font.getSplitter().splitLines(text, width, Style.EMPTY);
        boolean truncated = lines.size() > FOOTER_LINES;
        int count = Math.min(lines.size(), FOOTER_LINES);
        int y = FOOTER_Y + 2 + (FOOTER_LINES - count) * LINE_HEIGHT / 2;
        for (int i = 0; i < count; i++) {
            FormattedText line = lines.get(i);
            if (truncated && i == count - 1) {
                line = FormattedText.composite(font.substrByWidth(line, width - font.width(ELLIPSIS)), FormattedText.of(ELLIPSIS));
            }
            guiGraphics.drawString(font, Language.getInstance().getVisualOrder(line), guiLeft + PANEL_X + 2, guiTop + y, COLOR_TEXT_LIGHT, false);
            y += LINE_HEIGHT;
        }
        if (tooltip != null || truncated) {
            regions.add(new Region(PANEL_X, FOOTER_Y, PANEL_WIDTH, FOOTER_HEIGHT, button -> {}, tooltip != null ? tooltip : text));
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        if (draggingParameter) {
            renderDraggedParameter(guiGraphics, guiLeft, guiTop, fontRenderer, mouseX, mouseY);
            return;
        }
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();
        IOperator operator = element.getCurrentOperator();
        ExpressionEditor editor = getEditor();

        // Slot tooltips
        IValueType[] valueTypes = operator.getInputTypes();
        Container temporaryInputSlots = container.getTemporaryInputSlots();
        for (int i = 0; i < valueTypes.length && i < temporaryInputSlots.getContainerSize(); i++) {
            Pair<Integer, Integer> slotPosition = configRenderPattern.getSlotPositions()[i];
            if (gui.isHovering(getX() + slotPosition.getLeft(), getY() + slotPosition.getRight(),
                    ContainerScreenLogicProgrammerBase.BOX_HEIGHT, ContainerScreenLogicProgrammerBase.BOX_HEIGHT, mouseX, mouseY)) {
                List<Component> lines = Lists.newArrayList();
                if (editor != null) {
                    ExpressionNode child = editor.getChild(i);
                    // The tooltip of an item in the slot says what it is
                    if (!(child instanceof ExpressionNode.Empty) && !(child instanceof ExpressionNode.Ref) && temporaryInputSlots.getItem(i).isEmpty()) {
                        lines.add(Component.literal(ExpressionPrinter.printNode(editor.getExpression(),
                                ExpressionTrees.append(editor.getEditPath(), i), this::getLabel, editor.check())));
                    }
                    ExpressionTypeChecker.Error error = editor.check().getError(editor.getEditPath(), i);
                    if (error != null) {
                        lines.add(error.message().copy().withStyle(ChatFormatting.RED));
                    }
                }
                if (temporaryInputSlots.getItem(i).isEmpty()) {
                    lines.addAll(getValueTypeTooltip(valueTypes[i]));
                }
                if (!lines.isEmpty()) {
                    gui.drawTooltip(wrapTooltip(lines), guiGraphics.pose(), mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

        // Region tooltips
        for (Region region : regions) {
            if (region.tooltip() != null && gui.isHovering(region.x(), region.y(), region.width(), region.height(), mouseX, mouseY)) {
                gui.drawTooltip(wrapTooltip(Lists.newArrayList(region.tooltip())), guiGraphics.pose(), mouseX - guiLeft, mouseY - guiTop);
            }
        }

        // Output type tooltip
        this.drawTooltipForeground(gui, guiGraphics, container, guiLeft, guiTop, mouseX, mouseY,
                editor != null && editor.getExpression().hasParameters() ? ValueTypes.OPERATOR : editor != null ? editor.check().outputType() : operator.getOutputType());
    }

    /**
     * Draw the chip of the parameter that is being dragged under the mouse, above the items.
     */
    protected void renderDraggedParameter(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, int mouseX, int mouseY) {
        ExpressionEditor editor = getEditor();
        if (editor == null || pressedParameter >= editor.getExpression().parameters().size()) {
            return;
        }
        String name = editor.getExpression().parameters().get(pressedParameter).name();
        int width = font.width(name) + 4;
        int x = mouseX - guiLeft - width / 2;
        int y = mouseY - guiTop - CHIP_HEIGHT / 2;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 350);
        guiGraphics.fill(x, y, x + width, y + CHIP_HEIGHT, COLOR_PARAM_ARMED);
        drawOutline(guiGraphics, x, y, width, CHIP_HEIGHT, COLOR_SELECTED);
        guiGraphics.drawString(font, name, x + 2, y + 2, COLOR_TEXT, false);
        guiGraphics.pose().popPose();
    }

    protected static List<Component> wrapTooltip(List<Component> lines) {
        List<Component> wrapped = new ArrayList<>();
        for (Component line : lines) {
            String text = line.getString();
            if (text.length() <= L10NHelpers.MAX_TOOLTIP_LINE_LENGTH && !text.contains("\n")) {
                wrapped.add(line);
            } else {
                ChatFormatting format = line.getStyle().getColor() == null ? ChatFormatting.WHITE
                        : Objects.requireNonNullElse(ChatFormatting.getByName(line.getStyle().getColor().serialize()), ChatFormatting.WHITE);
                for (String paragraph : text.split("\n")) {
                    for (String part : StringHelpers.splitLines(paragraph, L10NHelpers.MAX_TOOLTIP_LINE_LENGTH, format.toString())) {
                        wrapped.add(Component.literal(part));
                    }
                }
            }
        }
        return wrapped;
    }

    @Override
    public boolean isRenderTooltip() {
        return this.renderTooltip;
    }

    @Override
    public void setRenderTooltip(boolean renderTooltip) {
        this.renderTooltip = renderTooltip;
    }

    protected static interface ClickAction {
        public void onClick(int mouseButton);
    }

    protected static record Region(int x, int y, int width, int height, ClickAction action, @Nullable Component tooltip) {
        public boolean contains(double x, double y) {
            return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height;
        }
    }

}
