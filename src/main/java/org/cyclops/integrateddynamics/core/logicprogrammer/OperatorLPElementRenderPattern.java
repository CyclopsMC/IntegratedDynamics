package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Container;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
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

    private static final int COLOR_TEXT = 0xFF404040;
    private static final int COLOR_TEXT_LIGHT = 0xFFE0E0E0;
    private static final int COLOR_ERROR = 0xFFE03030;
    private static final int COLOR_SELECTED = 0xFF3070FF;
    private static final int COLOR_PARAM = 0xFFE8C840;
    private static final int COLOR_CONST = 0xFFB0D0F0;
    private static final int COLOR_BADGE = 0xFFFF9020;
    private static final int COLOR_PARAM_ARMED = 0xFFFFF0A0;
    private static final int COLOR_TAB = 0xFFD8DCE0;
    private static final int COLOR_LINK = 0xFF2050A0;
    private static final int COLOR_STRIP = 0xFF373737;
    private static final int COLOR_STRIP_LINE = 0xFF555555;
    private static final int COLOR_STRIP_HOVER = 0xFF5A5A5A;
    private static final int DOUBLE_CLICK_MS = 350;

    // Side panel layout, relative to the gui
    private static final int PANEL_X = ContainerLogicProgrammerBase.EXPRESSION_PANEL_X + 6;
    private static final int PANEL_WIDTH = ContainerLogicProgrammerBase.EXPRESSION_PANEL_WIDTH - 12;
    private static final int TITLE_Y = ContainerLogicProgrammerBase.EXPRESSION_PANEL_Y + 6;
    private static final int PARAMS_Y = TITLE_Y + 12;
    private static final int PARAMS_ROWS = 2;
    private static final int STRIP_Y = PARAMS_Y + 12 + PARAMS_ROWS * 11 + 2;
    private static final int LINE_HEIGHT = 9;
    private static final int CONTENT_BOTTOM = ContainerLogicProgrammerBase.EXPRESSION_PANEL_Y + ContainerLogicProgrammerBase.EXPRESSION_PANEL_HEIGHT - 6;
    // The type, or an error instead, below the expression
    private static final int FOOTER_LINES = 3;
    private static final int FOOTER_HEIGHT = FOOTER_LINES * LINE_HEIGHT + 3;
    private static final int FOOTER_Y = CONTENT_BOTTOM - FOOTER_HEIGHT;
    private static final int STRIP_LINES = (FOOTER_Y - STRIP_Y - 3) / LINE_HEIGHT;
    private static final String ELLIPSIS = "\u2026";
    private static final float SMALL = 0.75F;

    // Editor layout, relative to the gui
    private static final int EDITOR_X = ContainerLogicProgrammerBase.BASE_X + 2;
    private static final int EDITOR_Y = ContainerLogicProgrammerBase.BASE_Y + ContainerLogicProgrammerBase.MAX_HEIGHT - 14;
    private static final int EDITOR_WIDTH = 70;

    private boolean renderTooltip = true;
    private final WidgetTextFieldExtended valueField;
    private int guiLeft;
    private int guiTop;
    private long lastClickTime = 0;
    private int lastClickSlot = -1;
    @Nullable
    private Component editorError = null;

    // Clickable regions, relative to the gui, recalculated while rendering
    private final List<Region> regions = new ArrayList<>();
    @Nullable
    private Button addParameterButton = null;

    public OperatorLPElementRenderPattern(OperatorLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                          ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.valueField = new WidgetTextFieldExtended(Minecraft.getInstance().font, 0, 0, EDITOR_WIDTH, 11,
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_VALUE, ""));
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
        this.valueField.setX(guiLeft + EDITOR_X);
        this.valueField.setY(guiTop + EDITOR_Y);
        updateEditor();

        buttonList.add(addParameterButton = new ButtonText(guiLeft + PANEL_X + PANEL_WIDTH - 12, guiTop + PARAMS_Y - 1, 12, 12,
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_ADD), Component.literal("+"),
                button -> sendAction(ExpressionEditor.actionAddParam()), true));
        addParameterButton.setTooltip(Tooltip.create(Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_ADD)));
        updateButtons();
    }

    /**
     * Show the buttons that apply to the current state, before they are rendered or clicked.
     */
    protected void updateButtons() {
        ExpressionEditor editor = getEditor();
        boolean panel = editor != null && ContainerScreenLogicProgrammerBase.isExpressionPanelOpen();
        if (addParameterButton != null) {
            addParameterButton.visible = panel && editor.getExpression().parameters().size() < ExpressionTrees.MAX_PARAMETERS;
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

    // --------------- Actions ---------------

    protected void sendAction(CompoundTag action) {
        container.applyExpressionAction(action);
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerExpressionActionPacket(action));
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
        if (editor == null || slot < 0) {
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
        for (Button button : Lists.newArrayList(buttonList)) {
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        if (valueField.isVisible()) {
            if (valueField.mouseClicked(mouseX, mouseY, mouseButton)) {
                valueField.setFocused(true);
                return true;
            }
            valueField.setFocused(false);
        }

        // Clickable regions (breadcrumb, parameters, strip)
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

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (valueField.isVisible() && valueField.isFocused()) {
            valueField.charTyped(typedChar, keyCode);
            editorError = null;
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (valueField.isVisible() && valueField.isFocused()) {
            if (typedChar == GLFW.GLFW_KEY_ENTER || typedChar == GLFW.GLFW_KEY_KP_ENTER) {
                submitEditor();
                return true;
            }
            valueField.keyPressed(typedChar, keyCode, modifiers);
            editorError = null;
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
                    if (!OperatorLPElement.isItemConstant(constant)) {
                        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_CONST);
                        drawFitted(guiGraphics, fontRenderer, ExpressionPrinter.printValue(constant.value()), x + 1, y + 1, 16, 16, COLOR_TEXT);
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
            } else if (element.getSelectedSlot() == i) {
                drawOutline(guiGraphics, x, y, 18, 18, COLOR_SELECTED);
            }
        }

        // Breadcrumb, as tabs for the operators from the root to the edited one
        if (editPath.length > 0) {
            int x = ContainerLogicProgrammerBase.BASE_X + 2;
            int y = ContainerLogicProgrammerBase.BASE_Y + 2;
            List<IOperator> operators = editor.getOperatorsAlongPath(editPath);
            for (int i = 0; i < operators.size(); i++) {
                int[] tabPath = Arrays.copyOf(editPath, i);
                if (i > 0) {
                    guiGraphics.drawString(fontRenderer, "\u203a", guiLeft + x + 1, guiTop + y + 2, COLOR_TEXT, false);
                    x += 8;
                }
                String text = OperatorNames.getNodeName((ExpressionNode.Operator) ExpressionTrees.get(editor.getExpression().root(), tabPath), tabPath, result);
                int width = fontRenderer.width(text) + 6;
                boolean current = i == operators.size() - 1;
                guiGraphics.fill(guiLeft + x, guiTop + y, guiLeft + x + width, guiTop + y + 12, current ? 0xFFFFFFFF : COLOR_TAB);
                drawOutline(guiGraphics, guiLeft + x, guiTop + y, width, 12, current ? COLOR_TEXT : 0xFF8B8B8B);
                guiGraphics.drawString(fontRenderer, text, guiLeft + x + 3, guiTop + y + 2, current ? COLOR_TEXT : COLOR_LINK, false);
                if (!current) {
                    regions.add(new Region(x, y, width, 12, button -> {
                        selectSlot(-1);
                        sendAction(ExpressionEditor.actionNavigate(tabPath));
                    }, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_BREADCRUMB)));
                }
                x += width;
            }
        }

        // Value editor
        if (valueField.isVisible()) {
            valueField.render(guiGraphics, mouseX, mouseY, partialTicks);
            int x = EDITOR_X + EDITOR_WIDTH + 3;
            int y = EDITOR_Y + 2;
            int armed = element.getArmedParameter();
            if (armed >= 0 && armed < editor.getExpression().parameters().size()) {
                renderParameterTypeCycler(guiGraphics, guiLeft, guiTop, fontRenderer, editor, armed, x, y);
            } else {
                Component hint = editorError != null ? editorError : getEditorHint(editor);
                int available = ContainerLogicProgrammerBase.BASE_X + ContainerLogicProgrammerBase.MAX_WIDTH - 2 - x;
                float scale = Math.min(1F, (float) available / fontRenderer.width(hint));
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(guiLeft + x, guiTop + y + (1 - scale) * 4, 300);
                guiGraphics.pose().scale(scale, scale, 1F);
                guiGraphics.drawString(fontRenderer, hint, 0, 0, editorError != null ? COLOR_ERROR : COLOR_TEXT, false);
                guiGraphics.pose().popPose();
            }
        }

        if (ContainerScreenLogicProgrammerBase.isExpressionPanelOpen()) {
            int hoveredSlot = getSlotAt(mouseX, mouseY);
            int[] hoveredPath = hoveredSlot >= 0 && hoveredSlot < editor.getSlotCount() ? ExpressionTrees.append(editPath, hoveredSlot) : null;
            renderParameters(guiGraphics, guiLeft, guiTop, fontRenderer, editor);
            renderStrip(guiGraphics, guiLeft, guiTop, fontRenderer, editor, result, hoveredPath);
            renderStatus(guiGraphics, guiLeft, guiTop, fontRenderer, editor.getExpression(), result);
        }
    }

    protected void renderParameterTypeCycler(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor,
                                             int index, int x, int y) {
        IValueType<?> type = editor.getExpression().parameters().get(index).type();
        Component typeName = Component.translatable(type.getTranslationKey());
        guiGraphics.drawString(font, "<", guiLeft + x, guiTop + y, COLOR_LINK, false);
        int nameX = x + font.width("<") + 3;
        guiGraphics.drawString(font, typeName, guiLeft + nameX, guiTop + y, COLOR_TEXT, false);
        int nextX = nameX + font.width(typeName) + 3;
        guiGraphics.drawString(font, ">", guiLeft + nextX, guiTop + y, COLOR_LINK, false);
        Component tooltip = Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_TYPE);
        regions.add(new Region(x - 1, y - 1, font.width("<") + 2, 10,
                button -> sendAction(ExpressionEditor.actionParamType(index, ExpressionEditor.cycleParameterType(type, -1))), tooltip));
        regions.add(new Region(nameX, y - 1, nextX + font.width(">") + 1 - nameX, 10,
                button -> sendAction(ExpressionEditor.actionParamType(index, ExpressionEditor.cycleParameterType(type, 1))), tooltip));
    }

    protected Component getEditorHint(ExpressionEditor editor) {
        IValueType<?> expectedType = editor.getExpectedType(element.getSelectedSlot());
        return Component.translatable(expectedType.getTranslationKey());
    }

    protected static void drawScaled(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 300);
        guiGraphics.pose().scale(SMALL, SMALL, 1F);
        guiGraphics.drawString(font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    protected static int scaledWidth(Font font, String text) {
        return (int) Math.ceil(font.width(text) * SMALL);
    }

    protected void renderParameters(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor) {
        guiGraphics.drawString(font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TITLE),
                guiLeft + PANEL_X, guiTop + TITLE_Y, COLOR_TEXT, false);

        drawScaled(guiGraphics, font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAMS),
                guiLeft + PANEL_X, guiTop + PARAMS_Y + 2, COLOR_TEXT);
        List<ExpressionParameter> parameters = editor.getExpression().parameters();

        int x = PANEL_X;
        int row = 0;
        for (int i = 0; i < parameters.size(); i++) {
            ExpressionParameter parameter = parameters.get(i);
            String text = parameter.name() + " : " + Component.translatable(parameter.type().getTranslationKey()).getString();
            int width = Math.min(scaledWidth(font, text) + 4, PANEL_WIDTH);
            if (x + width > PANEL_X + PANEL_WIDTH) {
                row++;
                x = PANEL_X;
            }
            if (row >= PARAMS_ROWS) {
                break;
            }
            int y = PARAMS_Y + 12 + row * 11;
            boolean armed = element.getArmedParameter() == i;
            guiGraphics.fill(guiLeft + x, guiTop + y, guiLeft + x + width, guiTop + y + 10, armed ? COLOR_PARAM_ARMED : COLOR_PARAM);
            drawOutline(guiGraphics, guiLeft + x, guiTop + y, width, 10, armed ? COLOR_SELECTED : 0xFF806010);
            drawScaled(guiGraphics, font, Component.literal(text), guiLeft + x + 2, guiTop + y + 2, COLOR_TEXT);
            int index = i;
            regions.add(new Region(x, y, width, 10, button -> {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    armParameter(-1);
                    sendAction(ExpressionEditor.actionRemoveParam(index));
                } else {
                    armParameter(element.getArmedParameter() == index ? -1 : index);
                    selectSlot(-1);
                }
            }, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_TOOLTIP)));
            x += width + 2;
        }
    }

    protected static boolean startsWith(int[] path, int[] prefix) {
        return path.length >= prefix.length && Arrays.equals(Arrays.copyOf(path, prefix.length), prefix);
    }

    protected void renderStrip(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor,
                               ExpressionTypeChecker.Result result, @Nullable int[] hoveredPath) {
        int x = PANEL_X;
        int y = STRIP_Y;
        guiGraphics.fill(guiLeft + x, guiTop + y, guiLeft + x + PANEL_WIDTH, guiTop + CONTENT_BOTTOM, COLOR_STRIP);
        Expression expression = editor.getExpression();
        List<ExpressionPrinter.Token> tokens = ExpressionPrinter.tokenize(expression, this::getLabel, result);
        int lineWidth = PANEL_WIDTH - 4;
        int tx = 0;
        int line = 0;
        for (ExpressionPrinter.Token token : tokens) {
            String text = token.text();
            int width = font.width(text);
            if (tx > 0 && tx + width > lineWidth) {
                line++;
                tx = 0;
                text = text.stripLeading();
                width = font.width(text);
            }
            if (line >= STRIP_LINES) {
                guiGraphics.drawString(font, ELLIPSIS, guiLeft + x + PANEL_WIDTH - 2 - font.width(ELLIPSIS),
                        guiTop + y + 2 + (STRIP_LINES - 1) * LINE_HEIGHT, COLOR_TEXT_LIGHT, false);
                break;
            }
            int[] path = token.path();
            int color = COLOR_TEXT_LIGHT;
            int tokenX = x + 2 + tx;
            int tokenY = y + 2 + line * LINE_HEIGHT;
            if (path != null) {
                int[] nodePath = ExpressionTrees.isOperatorPath(expression.root(), path) ? path : ExpressionTrees.parent(path);
                if (Arrays.equals(nodePath, editor.getEditPath())) {
                    color = 0xFFFFFF60;
                }
                if (result.hasErrorAt(nodePath) && (path == nodePath || result.getError(nodePath, path[path.length - 1]) != null)) {
                    color = 0xFFFF6060;
                }
                if (text.equals(ExpressionPrinter.EMPTY)) {
                    color = 0xFFA0A0A0;
                }
                if (hoveredPath != null && startsWith(path, hoveredPath)) {
                    guiGraphics.fill(guiLeft + tokenX, guiTop + tokenY - 1, guiLeft + tokenX + width, guiTop + tokenY + LINE_HEIGHT - 1, COLOR_STRIP_HOVER);
                }
                regions.add(new Region(tokenX, tokenY, width, LINE_HEIGHT, button -> {
                    selectSlot(-1);
                    sendAction(ExpressionEditor.actionNavigate(nodePath));
                }, null));
            }
            guiGraphics.drawString(font, text, guiLeft + tokenX, guiTop + tokenY, color, false);
            tx += width;
        }
    }

    /**
     * Draw the type of the expression in the footer, in the colors of its types as in tooltips,
     * or the first error instead, which keeps it from being written.
     * The check mark next to the operator name already tells when it can be written.
     */
    protected void renderStatus(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, Expression expression,
                                ExpressionTypeChecker.Result result) {
        if (!result.errors().isEmpty()) {
            ExpressionTypeChecker.Error error = result.errors().get(0);
            Component status = error.message();
            if (error.slot() >= 0) {
                String operatorName = ExpressionTrees.get(expression.root(), error.path()) instanceof ExpressionNode.Operator op
                        ? OperatorNames.getNodeName(op, error.path(), result) : "?";
                status = Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_STATUS_ERRORS_SLOT, result.errors().size(), error.slot() + 1, operatorName);
            }
            renderFooter(guiGraphics, guiLeft, guiTop, font, status.copy().withStyle(ChatFormatting.RED),
                    error.message().copy().withStyle(ChatFormatting.RED));
            return;
        }
        IValueType<?> outputType = result.outputType();
        Component type = expression.hasParameters()
                ? ValueTypeOperator.getSignature(ExpressionTypeChecker.getInputTypes(expression), outputType)
                : Component.translatable(outputType.getTranslationKey()).withStyle(outputType.getDisplayColorFormat());
        renderFooter(guiGraphics, guiLeft, guiTop, font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TYPE, type), null);
    }

    /**
     * Draw text centered in the footer, on the dark background of the expression where the colors of types are readable.
     * Text that does not fit ends in an ellipsis.
     * @param tooltip Shown when hovering the footer, defaults to the text if it does not fit.
     */
    protected void renderFooter(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, Component text, @Nullable Component tooltip) {
        guiGraphics.fill(guiLeft + PANEL_X, guiTop + FOOTER_Y, guiLeft + PANEL_X + PANEL_WIDTH, guiTop + CONTENT_BOTTOM, COLOR_STRIP);
        guiGraphics.fill(guiLeft + PANEL_X + 2, guiTop + FOOTER_Y, guiLeft + PANEL_X + PANEL_WIDTH - 2, guiTop + FOOTER_Y + 1, COLOR_STRIP_LINE);
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
                    if (!(child instanceof ExpressionNode.Empty) && !(child instanceof ExpressionNode.Ref) && !OperatorLPElement.isItemConstant(child)) {
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

    protected static List<Component> wrapTooltip(List<Component> lines) {
        List<Component> wrapped = new ArrayList<>();
        for (Component line : lines) {
            String text = line.getString();
            if (text.length() <= L10NHelpers.MAX_TOOLTIP_LINE_LENGTH) {
                wrapped.add(line);
            } else {
                ChatFormatting format = line.getStyle().getColor() == null ? ChatFormatting.WHITE
                        : Objects.requireNonNullElse(ChatFormatting.getByName(line.getStyle().getColor().serialize()), ChatFormatting.WHITE);
                for (String part : StringHelpers.splitLines(text, L10NHelpers.MAX_TOOLTIP_LINE_LENGTH, format.toString())) {
                    wrapped.add(Component.literal(part));
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
