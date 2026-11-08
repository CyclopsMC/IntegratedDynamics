package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.Container;
import org.apache.commons.lang3.tuple.Pair;
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
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionEditor;
import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionHistory;
import org.cyclops.integrateddynamics.core.logicprogrammer.IRenderPatternValueTypeTooltip;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.Region;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParseException;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParser;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTypeChecker;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.OperatorNames;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerExpressionActionPacket;
import com.mojang.blaze3d.platform.InputConstants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_EDITED;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_EMPTY;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_ERROR;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_SELECTED;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_STRIP_HOVER;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_TEXT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_TEXT_ERROR;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_TEXT_LIGHT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.CONTENT_BOTTOM;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.FOOTER_Y;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.PANEL_WIDTH;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.PANEL_X;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.drawFitted;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.drawOutline;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.getContrastingTextColor;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.wrapTooltip;

/**
 * Render pattern for operator elements, which edits the expression of the element in the slots and in the expression panel.
 * The parts of the panel are in {@link ExpressionParameterChips}, {@link ExpressionTextController},
 * {@link ExpressionBreadcrumb} and {@link ExpressionValueEditor}.
 * @author rubensworks
 */
public class OperatorLPElementRenderPattern extends RenderPattern<OperatorLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
        implements IRenderPatternValueTypeTooltip {

    private static final int COLOR_CONST = ARGB.color(176, 208, 240);
    private static final int COLOR_BADGE = ARGB.color(255, 144, 32);
    private static final int COLOR_BADGE_BACKGROUND = ARGB.color(0, 0, 0);
    private static final int DOUBLE_CLICK_MS = 350;

    private final Text text;
    private final ExpressionParameterChips chips;
    private final ExpressionValueEditor valueEditor;
    private boolean renderTooltip = true;
    private int guiLeft;
    private int guiTop;
    private long lastClickTime = 0;
    private int lastClickSlot = -1;
    // Clickable regions, relative to the gui, recalculated while rendering
    private final List<Region> regions = new ArrayList<>();
    // Undo, Redo, Format, Copy and adding a parameter
    @Nullable
    private ExpressionPanelButtons buttons = null;
    private int textBoxY = FOOTER_Y;

    public OperatorLPElementRenderPattern(OperatorLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                          ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.text = new Text(container);
        this.chips = new ExpressionParameterChips(this, element);
        this.valueEditor = new ExpressionValueEditor(this, element);
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        valueEditor.init(guiLeft, guiTop);
        updateEditor();
        buttons = new ExpressionPanelButtons(() -> getEditor() != null && getEditor().getExpression().parameters().isEmpty(),
                this::addParameter, text::undo, text::redo, text::formatText, text::copyText);
        buttonList.addAll(buttons.getAll());
        updateButtons();
    }

    /**
     * Show the buttons that apply to the current state, before they are rendered or clicked.
     */
    protected void updateButtons() {
        ExpressionEditor editor = getEditor();
        boolean panel = editor != null && ContainerScreenLogicProgrammerBase.isExpressionPanelOpen();
        boolean draft = panel && text.isDraft();
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
        buttons.format.active = draft ? text.parseText() != null : panel && editor.getText() != null;
        buttons.copy.visible = panel;
        buttons.layout(guiLeft, guiTop);
        if (editor != null) {
            int[] button = ExpressionParameterChips.layout(Minecraft.getInstance().font, editor.getExpression().parameters(),
                    buttons.addParameter.getWidth()).button();
            buttons.addParameter.setPosition(guiLeft + button[0], guiTop + button[1]);
        }
    }

    /**
     * Add a parameter, to the typed text while it is not applied yet, as applying it would undo the parameter otherwise.
     */
    protected void addParameter() {
        if (text.isDraft()) {
            text.focusText();
            getTextField().addParameter(text.getParserContext());
        } else {
            sendAction(ExpressionEditor.actionAddParam());
        }
    }

    @Override
    protected String getRenderSymbol() {
        return element.getCurrentOperator().getSymbol();
    }

    @Nullable
    protected ExpressionEditor getEditor() {
        return element.getEditor();
    }

    protected void sendAction(CompoundTag action) {
        container.applyExpressionAction(action);
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerExpressionActionPacket(action));
    }

    protected ExpressionTextField getTextField() {
        return text.getTextField();
    }

    // --------------- Selection ---------------

    protected void selectSlot(int slot) {
        element.setSelectedSlot(slot);
        container.setExpressionSlotFilter(slot >= 0 && getEditor() != null ? getEditor().getExpectedType(slot) : null);
        updateEditor();
    }

    protected void armParameter(int index) {
        element.setArmedParameter(index);
        updateEditor();
    }

    protected void updateEditor() {
        valueEditor.update(getEditor());
    }

    /**
     * Stop typing, or cancel the selected slot or parameter.
     * @return If something was stopped or cancelled.
     */
    public boolean cancelSelection() {
        if (text.cancel()) {
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
     * Called by the gui when an operator was picked from the list while a slot is selected.
     * @param operator The operator.
     * @return If the operator was nested.
     */
    public boolean onOperatorPicked(IOperator operator) {
        ExpressionEditor editor = getEditor();
        int slot = element.getSelectedSlot();
        if (editor == null || slot < 0 || text.isDraft()) {
            return false;
        }
        if (ExpressionEditor.isPickedAsValue(editor.getExpectedType(slot), operator, Minecraft.getInstance().hasShiftDown())) {
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

    // --------------- Text ---------------

    /**
     * Apply typed text once typing pauses. Called each tick, outside of rendering.
     */
    public void tickText() {
        text.tick();
    }

    /**
     * Apply typed text that is not applied yet, such as when the gui closes.
     */
    public void applyPendingText() {
        text.applyPendingText();
    }

    /**
     * @return Why typed text can not be applied, which keeps the card from being written, or null if there is none.
     */
    @Nullable
    public ExpressionParseException getDraftError() {
        return text.getDraftError();
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

    protected boolean isInTextBox(double mouseX, double mouseY) {
        return ContainerScreenLogicProgrammerBase.isExpressionPanelOpen() && getEditor() != null
                && mouseX >= guiLeft + PANEL_X && mouseX < guiLeft + PANEL_X + PANEL_WIDTH
                && mouseY >= guiTop + textBoxY && mouseY < guiTop + FOOTER_Y;
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
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        ExpressionEditor editor = getEditor();
        if (editor == null) {
            return super.mouseClicked(evt, isDoubleClick);
        }
        double mouseX = evt.x();
        double mouseY = evt.y();
        int mouseButton = evt.button();
        chips.onMousePressed(mouseX, mouseY);
        ExpressionTextField field = getTextField();
        field.setPressed(false);
        // The list shown while typing, which can cover the rest of the panel
        if (field.mouseClickedCompletion(mouseX - guiLeft - PANEL_X - 2, mouseY - guiTop - textBoxY - 2)) {
            return true;
        }
        for (Button button : Lists.newArrayList(buttonList)) {
            if (button.mouseClicked(evt, isDoubleClick)) {
                return true;
            }
        }

        // The text of the expression, where clicking a part also opens it in the slots
        if (isInTextBox(mouseX, mouseY)) {
            text.focusText();
            field.mouseClicked(mouseX - guiLeft - PANEL_X - 2, mouseY - guiTop - textBoxY - 2);
            field.setPressed(true);
            navigateToCursor();
            return true;
        }
        field.setFocused(false);
        // Slots, parameters and tabs would be replaced when applying the text, so valid text is applied first
        if (field.isDraft()) {
            if (text.parseText() != null) {
                text.applyText();
                return true;
            }
            return false;
        }

        if (valueEditor.mouseClicked(editor, evt, isDoubleClick)) {
            return true;
        }

        // Breadcrumb and parameters
        for (Region region : Lists.newArrayList(regions)) {
            if (region.contains(mouseX - guiLeft, mouseY - guiTop)) {
                region.action().onClick(mouseButton);
                return true;
            }
        }

        int slot = getSlotAt(mouseX, mouseY);
        if (slot >= 0 && slot < editor.getSlotCount() && container.getCarried().isEmpty()) {
            if (mouseButton == InputConstants.MOUSE_BUTTON_RIGHT) {
                sendAction(ExpressionEditor.actionClear(editor.getEditPath(), slot));
                selectSlot(-1);
                return true;
            }
            if (mouseButton == InputConstants.MOUSE_BUTTON_LEFT) {
                onSlotClicked(editor, slot);
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
        return super.mouseClicked(evt, isDoubleClick);
    }

    /**
     * Put the selected parameter in the slot, or open the nested operator in it when it is clicked twice, or select it.
     */
    protected void onSlotClicked(ExpressionEditor editor, int slot) {
        long now = Util.getMillis();
        boolean doubleClick = lastClickSlot == slot && now - lastClickTime < DOUBLE_CLICK_MS;
        lastClickTime = now;
        lastClickSlot = slot;
        int armed = element.getArmedParameter();
        if (armed >= 0) {
            sendAction(ExpressionEditor.actionParam(editor.getEditPath(), slot, armed));
            armParameter(-1);
            selectSlot(-1);
        } else if (editor.getChild(slot) instanceof ExpressionNode.Operator && (doubleClick || element.getSelectedSlot() == slot)) {
            selectSlot(-1);
            sendAction(ExpressionEditor.actionNavigate(ExpressionTrees.append(editor.getEditPath(), slot)));
        } else {
            selectSlot(element.getSelectedSlot() == slot ? -1 : slot);
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (getTextField().isPressed()) {
            getTextField().mouseDragged(mouseX - guiLeft - PANEL_X - 2, mouseY - guiTop - textBoxY - 2);
            return true;
        }
        return chips.mouseDragged(mouseX, mouseY, button);
    }

    /**
     * Drop a dragged parameter into the slot under the mouse, or cancel when it is not over a slot.
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (getTextField().isPressed()) {
            getTextField().setPressed(false);
            return true;
        }
        int index = chips.mouseReleased();
        ExpressionEditor editor = getEditor();
        if (index < 0 || editor == null) {
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

    @Override
    public boolean charTyped(CharacterEvent evt) {
        return text.charTyped(evt) || valueEditor.charTyped(evt) || super.charTyped(evt);
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (text.keyPressed(evt) || valueEditor.keyPressed(getEditor(), evt)) {
            return true;
        }
        if (ExpressionTextController.isUndoKey(evt)) {
            text.undo();
            return true;
        }
        if (ExpressionTextController.isRedoKey(evt)) {
            text.redo();
            return true;
        }
        return super.keyPressed(evt);
    }

    // --------------- Rendering ---------------

    @Override
    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        updateButtons();
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
        regions.clear();
        ExpressionEditor editor = getEditor();
        if (editor == null) {
            return;
        }
        ExpressionTypeChecker.Result result = editor.check();
        renderSlots(guiGraphics, guiLeft, guiTop, fontRenderer, editor, result, mouseX, mouseY);
        ExpressionBreadcrumb.render(guiGraphics, guiLeft, guiTop, fontRenderer, editor, result, path -> {
            selectSlot(-1);
            sendAction(ExpressionEditor.actionNavigate(path));
        }, regions);
        valueEditor.render(guiGraphics, guiLeft, guiTop, fontRenderer, editor, mouseX, mouseY, partialTicks);

        // Changes from slots, parameters and text alike can be undone
        container.getExpressionHistory().observe(editor.getExpression(), editor.getText());
        ExpressionTextField field = getTextField();
        field.sync(editor.getExpression(), editor.getText(), () -> text.printTokens(editor.getExpression(), editor.getText()));
        if (ContainerScreenLogicProgrammerBase.isExpressionPanelOpen()) {
            ExpressionPanelRendering.renderTitle(guiGraphics, guiLeft, guiTop, fontRenderer);
            int hoveredSlot = getSlotAt(mouseX, mouseY);
            int[] hoveredPath = hoveredSlot >= 0 && hoveredSlot < editor.getSlotCount() ? ExpressionTrees.append(editor.getEditPath(), hoveredSlot) : null;
            textBoxY = chips.render(guiGraphics, guiLeft, guiTop, fontRenderer, editor, buttons.addParameter.getWidth(), regions);
            renderTextField(guiGraphics, guiLeft, guiTop, editor, result, hoveredPath, mouseX, mouseY);
            // The signature of the call at the cursor is shown above its line instead, so this always describes the expression
            if (!field.isDraft()) {
                ExpressionPanelRendering.renderStatus(guiGraphics, guiLeft, guiTop, fontRenderer, editor.getExpression(), result, regions);
            } else if (text.parseText() != null) {
                ExpressionPanelRendering.renderStatus(guiGraphics, guiLeft, guiTop, fontRenderer, text.parseText(), text.getParsedResult(), regions);
            } else {
                ExpressionPanelRendering.renderTextError(guiGraphics, guiLeft, guiTop, fontRenderer, text.getParseError(), regions);
            }
        }
    }

    /**
     * Draw the contents of the slots that are not items: nested operators, values, and parameters.
     */
    protected void renderSlots(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor,
                               ExpressionTypeChecker.Result result, int mouseX, int mouseY) {
        int[] editPath = editor.getEditPath();
        int dropSlot = chips.isDragging() ? getSlotAt(mouseX, mouseY) : -1;
        Pair<Integer, Integer>[] slotPositions = element.getRenderPattern().getSlotPositions();
        for (int i = 0; i < slotPositions.length && i < editor.getSlotCount(); i++) {
            int x = guiLeft + getX() + slotPositions[i].getLeft();
            int y = guiTop + getY() + slotPositions[i].getRight();
            switch (editor.getChild(i)) {
                case ExpressionNode.Operator op -> {
                    int background = ARGB.opaque(op.operator().getOutputType().getDisplayColor());
                    guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, background);
                    drawFitted(guiGraphics, font, OperatorNames.getNodeName(op, ExpressionTrees.append(editPath, i), result), x + 1, y + 1, 16, 16,
                            getContrastingTextColor(background));
                    if (ExpressionTrees.countEmpty(op) > 0) {
                        // Badge for unfilled inputs
                        guiGraphics.fill(x + 12, y + 1, x + 17, y + 6, COLOR_BADGE_BACKGROUND);
                        guiGraphics.fill(x + 13, y + 2, x + 16, y + 5, COLOR_BADGE);
                    }
                }
                case ExpressionNode.Const constant -> {
                    if (OperatorLPElement.getItemOfValue(constant).isEmpty()) {
                        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_CONST);
                        drawFitted(guiGraphics, font, getConstantText(constant.value()), x + 1, y + 1, 16, 16, COLOR_TEXT);
                    }
                }
                case ExpressionNode.Param param -> {
                    guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, ExpressionParameterChips.COLOR_PARAM);
                    ExpressionParameter parameter = editor.getExpression().parameters().get(param.index());
                    drawFitted(guiGraphics, font, parameter.name(), x + 1, y + 1, 16, 16, COLOR_TEXT);
                }
                default -> {}
            }
            if (result.getError(editPath, i) != null || result.hasErrorWithin(ExpressionTrees.append(editPath, i))) {
                drawOutline(guiGraphics, x, y, 18, 18, COLOR_ERROR);
            } else if (element.getSelectedSlot() == i || dropSlot == i) {
                drawOutline(guiGraphics, x, y, 18, 18, COLOR_SELECTED);
            }
        }
    }

    // In brackets for an operator, as a nested operator shows the same name
    private static String getConstantText(IValue value) {
        String text = ExpressionPrinter.printValue(value);
        return value instanceof ValueTypeOperator.ValueOperator && !text.startsWith("(") ? "(" + text + ")" : text;
    }

    protected static boolean startsWith(int[] path, int[] prefix) {
        return path.length >= prefix.length && Arrays.equals(Arrays.copyOf(path, prefix.length), prefix);
    }

    /**
     * Draw the text of the expression, which can be edited in place.
     * It is colored like the slots: the edited operator, errors, empty slots, and the part of a hovered slot.
     */
    protected void renderTextField(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, ExpressionEditor editor,
                                   ExpressionTypeChecker.Result result, @Nullable int[] hoveredPath, int mouseX, int mouseY) {
        ExpressionTextField field = getTextField();
        field.setParserContext(text::getParserContext);
        int x = guiLeft + PANEL_X;
        int y = guiTop + textBoxY;
        ExpressionPanelRendering.renderTextBox(guiGraphics, x, y, guiTop + FOOTER_Y, guiTop + CONTENT_BOTTOM, field.isFocused());

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
        ExpressionParseException error = draft ? text.getParseError() : null;
        int mark = -1;
        int markEnd = -1;
        if (error != null) {
            mark = Math.min(error.getPosition(), field.getValue().length());
            markEnd = Math.min(error.getEnd(), field.getValue().length());
        }
        field.render(guiGraphics, x + 2, y + 2, PANEL_WIDTH - 3, guiTop + FOOTER_Y - y - 3, colors, backgrounds, mark, markEnd, COLOR_TEXT_ERROR);
        if (error != null) {
            field.renderErrorTooltip(guiGraphics, mark, markEnd, error.getComponent(), mouseX, mouseY);
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

    @Override
    public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        ExpressionEditor editor = getEditor();
        if (chips.isDragging() && editor != null) {
            chips.renderDragged(guiGraphics, guiLeft, guiTop, fontRenderer, editor, mouseX, mouseY);
            return;
        }
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();
        IOperator operator = element.getCurrentOperator();

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
                                ExpressionTrees.append(editor.getEditPath(), i), text::getLabel, editor.check())));
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
                    gui.drawTooltip(wrapTooltip(lines), guiGraphics, mouseX, mouseY);
                }
            }
        }

        ExpressionPanelRendering.renderRegionTooltips(gui, guiGraphics, regions, mouseX, mouseY);

        // Output type tooltip
        this.drawTooltipForeground(gui, guiGraphics, container, guiLeft, guiTop, mouseX, mouseY,
                editor != null && editor.getExpression().hasParameters() ? ValueTypes.OPERATOR : editor != null ? editor.check().outputType() : operator.getOutputType());
    }

    @Override
    public boolean isRenderTooltip() {
        return this.renderTooltip;
    }

    @Override
    public void setRenderTooltip(boolean renderTooltip) {
        this.renderTooltip = renderTooltip;
    }

    /**
     * The text of the expression of the element, which applies typed text to the element.
     */
    private class Text extends ExpressionTextController {

        private Text(ContainerLogicProgrammerBase container) {
            super(container);
        }

        @Nullable
        @Override
        protected ExpressionEditor getEditor() {
            return OperatorLPElementRenderPattern.this.getEditor();
        }

        @Override
        public void focusText() {
            super.focusText();
            selectSlot(-1);
            armParameter(-1);
            valueEditor.setFocused(false);
        }

        /**
         * Apply the typed text if it parses. It stays as typed, also on the card, unless it is the canonical form.
         * The same expression typed in another way is applied too, as its text is kept.
         */
        @Override
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
         * Show the expression in its canonical form instead of as typed, applying typed text first if it parses.
         */
        @Override
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

        @Override
        protected void undo() {
            if (isDraft()) {
                getTextField().revert();
                return;
            }
            ExpressionHistory.Entry entry = container.getExpressionHistory().undo();
            if (entry != null) {
                setExpressionFromHistory(entry);
            }
        }

        @Override
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
        private void setExpressionFromHistory(ExpressionHistory.Entry entry) {
            sendAction(ExpressionEditor.actionSet(ValueDeseralizationContext.ofClient(), entry.expression(), entry.text()));
            if (container.getActiveElement() instanceof OperatorLPElement active && active.getEditor() != null) {
                container.getExpressionHistory().moved(new ExpressionHistory.Entry(active.getEditor().getExpression(), active.getEditor().getText()));
            }
        }

        /**
         * Also show the text of the expression in place of applied text once it loses focus.
         */
        @Override
        public void tick() {
            if (getEditor() == null) {
                return;
            }
            super.tick();
            if (!getTextField().isFocused()) {
                getTextField().showExpressionText();
            }
        }

        /**
         * @param expression The expression.
         * @param text The text it was typed as, or null.
         * @return The tokens of the text if it still reads as the expression, and otherwise of the expression in its canonical form,
         *         such as after a label that the text uses changed.
         */
        private List<ExpressionPrinter.Token> printTokens(Expression expression, @Nullable String text) {
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
    }

}
