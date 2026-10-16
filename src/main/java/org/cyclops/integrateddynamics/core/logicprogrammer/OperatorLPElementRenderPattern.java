package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
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
    private static final int COLOR_OK = 0xFF208020;
    private static final int COLOR_SELECTED = 0xFF3070FF;
    private static final int COLOR_PARAM = 0xFFE8C840;
    private static final int COLOR_CONST = 0xFFB0D0F0;
    private static final int DOUBLE_CLICK_MS = 350;
    private static final int MAX_DEPTH_WARNING = 4;

    // Band layout, relative to the gui
    // Side panel layout, relative to the gui
    private static final int PANEL_X = ContainerLogicProgrammerBase.EXPRESSION_PANEL_X + 6;
    private static final int PANEL_WIDTH = ContainerLogicProgrammerBase.EXPRESSION_PANEL_WIDTH - 12;
    private static final int TITLE_Y = ContainerLogicProgrammerBase.EXPRESSION_PANEL_Y + 6;
    private static final int PARAMS_Y = TITLE_Y + 12;
    private static final int PARAMS_ROWS = 2;
    private static final int STRIP_Y = PARAMS_Y + 12 + PARAMS_ROWS * 11 + 2;
    private static final int STRIP_LINES = 6;
    private static final int STRIP_LINE_HEIGHT = 8;
    private static final int STATUS_Y = STRIP_Y + STRIP_LINES * STRIP_LINE_HEIGHT + 6;
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
        sendAction(ExpressionEditor.actionOperator(editor.getEditPath(), slot, operator));
        selectSlot(-1);
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
            if (typedChar == GLFW.GLFW_KEY_ESCAPE) {
                selectSlot(-1);
                armParameter(-1);
                return true;
            }
            valueField.keyPressed(typedChar, keyCode, modifiers);
            editorError = null;
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    // --------------- Rendering ---------------

    protected static void drawSmallString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int maxWidth, int color) {
        float scale = 0.5F;
        int width = font.width(text);
        if (width * scale > maxWidth) {
            while (text.length() > 1 && font.width(text + "…") * scale > maxWidth) {
                text = text.substring(0, text.length() - 1);
            }
            text = text + "…";
            width = font.width(text);
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + (maxWidth - width * scale) / 2F, y, 300);
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
                    int textColor = getContrastingTextColor(background);
                    guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, background);
                    drawSmallString(guiGraphics, fontRenderer, op.operator().getSymbol(), x + 1, y + 4, 16, textColor);
                    if (ExpressionTrees.countEmpty(op) > 0) {
                        drawSmallString(guiGraphics, fontRenderer, "( ? )", x + 1, y + 11, 16, textColor);
                    }
                }
                case ExpressionNode.Const constant -> {
                    if (!OperatorLPElement.isItemConstant(constant)) {
                        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_CONST);
                        drawSmallString(guiGraphics, fontRenderer, ExpressionPrinter.printValue(constant.value()), x + 1, y + 7, 16, COLOR_TEXT);
                    }
                }
                case ExpressionNode.Param param -> {
                    guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_PARAM);
                    ExpressionParameter parameter = editor.getExpression().parameters().get(param.index());
                    drawSmallString(guiGraphics, fontRenderer, parameter.name(), x + 1, y + 7, 16, COLOR_TEXT);
                }
                default -> {}
            }
            if (result.getError(editPath, i) != null) {
                drawOutline(guiGraphics, x, y, 18, 18, COLOR_ERROR);
            } else if (element.getSelectedSlot() == i) {
                drawOutline(guiGraphics, x, y, 18, 18, COLOR_SELECTED);
            }
        }

        // Breadcrumb
        if (editPath.length > 0) {
            int x = ContainerLogicProgrammerBase.BASE_X + 2;
            int y = ContainerLogicProgrammerBase.BASE_Y + 2;
            List<IOperator> operators = editor.getOperatorsAlongPath(editPath);
            for (int i = 0; i < operators.size(); i++) {
                String text = (i == 0 ? "" : " / ") + operators.get(i).getSymbol();
                int width = fontRenderer.width(text);
                boolean current = i == operators.size() - 1;
                guiGraphics.drawString(fontRenderer, text, guiLeft + x, guiTop + y, current ? COLOR_TEXT : 0xFF2050A0, false);
                if (!current) {
                    int[] targetPath = Arrays.copyOf(editPath, i);
                    regions.add(new Region(x, y, width, 9, button -> {
                        selectSlot(-1);
                        sendAction(ExpressionEditor.actionNavigate(targetPath));
                    }, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_BREADCRUMB)));
                }
                x += width;
            }
        }

        // Value editor
        if (valueField.isVisible()) {
            valueField.render(guiGraphics, mouseX, mouseY, partialTicks);
            Component hint = editorError != null ? editorError : getEditorHint(editor);
            guiGraphics.drawString(fontRenderer, hint, guiLeft + EDITOR_X + EDITOR_WIDTH + 3, guiTop + EDITOR_Y + 2,
                    editorError != null ? COLOR_ERROR : COLOR_TEXT, false);
        } else if (element.getSelectedSlot() >= 0 && element.getSelectedSlot() < editor.getSlotCount()) {
            IValueType<?> expectedType = editor.getExpectedType(element.getSelectedSlot());
            Component hint = ValueHelpers.correspondsTo(expectedType, ValueTypes.OBJECT_ITEMSTACK)
                    ? Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_VALUE_ITEM)
                    : Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_VALUE_NONE);
            drawSmallString(guiGraphics, fontRenderer, hint.getString(), guiLeft + EDITOR_X, guiTop + EDITOR_Y + 3, ContainerLogicProgrammerBase.MAX_WIDTH - 4, COLOR_TEXT);
        }

        renderParameters(guiGraphics, guiLeft, guiTop, fontRenderer, editor);
        renderStrip(guiGraphics, guiLeft, guiTop, fontRenderer, editor, result);
        renderStatus(guiGraphics, guiLeft, guiTop, fontRenderer, editor, result);
    }

    protected Component getEditorHint(ExpressionEditor editor) {
        if (element.getArmedParameter() >= 0) {
            return Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_NAME);
        }
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
        if (parameters.size() < ExpressionTrees.MAX_PARAMETERS) {
            int addX = PANEL_X + PANEL_WIDTH - 10;
            guiGraphics.fill(guiLeft + addX, guiTop + PARAMS_Y, guiLeft + addX + 10, guiTop + PARAMS_Y + 10, 0xFF8B8B8B);
            drawOutline(guiGraphics, guiLeft + addX, guiTop + PARAMS_Y, 10, 10, 0xFF373737);
            guiGraphics.drawString(font, "+", guiLeft + addX + 2, guiTop + PARAMS_Y + 1, COLOR_TEXT_LIGHT, false);
            regions.add(new Region(addX, PARAMS_Y, 10, 10, button -> sendAction(ExpressionEditor.actionAddParam()),
                    Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_ADD)));
        }

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
            guiGraphics.fill(guiLeft + x, guiTop + y, guiLeft + x + width, guiTop + y + 10, armed ? COLOR_SELECTED : COLOR_PARAM);
            drawOutline(guiGraphics, guiLeft + x, guiTop + y, width, 10, 0xFF806010);
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

    protected void renderStrip(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor,
                               ExpressionTypeChecker.Result result) {
        int x = PANEL_X;
        int y = STRIP_Y;
        int height = STRIP_LINES * STRIP_LINE_HEIGHT + 3;
        guiGraphics.fill(guiLeft + x, guiTop + y, guiLeft + x + PANEL_WIDTH, guiTop + y + height, 0xFF373737);
        Expression expression = editor.getExpression();
        List<ExpressionPrinter.Token> tokens = ExpressionPrinter.tokenize(expression, this::getLabel);
        int lineWidth = PANEL_WIDTH - 4;
        int tx = 0;
        int line = 0;
        for (ExpressionPrinter.Token token : tokens) {
            String text = token.text();
            int width = scaledWidth(font, text);
            if (tx > 0 && tx + width > lineWidth) {
                line++;
                tx = 0;
                text = text.stripLeading();
                width = scaledWidth(font, text);
            }
            if (line >= STRIP_LINES) {
                drawScaled(guiGraphics, font, Component.literal("…"), guiLeft + x + PANEL_WIDTH - 8,
                        guiTop + y + 2 + (STRIP_LINES - 1) * STRIP_LINE_HEIGHT, COLOR_TEXT_LIGHT);
                break;
            }
            int[] path = token.path();
            int color = COLOR_TEXT_LIGHT;
            int tokenX = x + 2 + tx;
            int tokenY = y + 2 + line * STRIP_LINE_HEIGHT;
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
                regions.add(new Region(tokenX, tokenY, width, STRIP_LINE_HEIGHT, button -> {
                    selectSlot(-1);
                    sendAction(ExpressionEditor.actionNavigate(nodePath));
                }, null));
            }
            drawScaled(guiGraphics, font, Component.literal(text), guiLeft + tokenX, guiTop + tokenY, color);
            tx += width;
        }
    }

    protected void renderStatus(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor,
                                ExpressionTypeChecker.Result result) {
        Expression expression = editor.getExpression();
        MutableComponent type;
        if (expression.hasParameters()) {
            type = ValueTypeOperator.getSignature(
                    ExpressionTypeChecker.getInputTypes(expression), result.outputType()).copy();
        } else {
            type = Component.translatable(result.outputType().getTranslationKey());
        }
        MutableComponent status;
        int color;
        if (!result.errors().isEmpty()) {
            ExpressionTypeChecker.Error error = result.errors().get(0);
            status = error.slot() >= 0
                    ? Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_STATUS_ERRORS_SLOT, result.errors().size(), error.slot() + 1)
                    : Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_STATUS_ERRORS, result.errors().size());
            color = COLOR_ERROR;
        } else if (result.emptySlots() > 0) {
            status = Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_STATUS_EMPTY, result.emptySlots());
            color = COLOR_TEXT;
        } else {
            status = Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_STATUS_READY);
            color = COLOR_OK;
        }
        int depth = ExpressionTrees.depth(expression.root());
        if (depth > MAX_DEPTH_WARNING) {
            status = status.append(" ").append(Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_STATUS_DEEP, depth));
        }
        drawScaled(guiGraphics, font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TYPE, type),
                guiLeft + PANEL_X, guiTop + STATUS_Y, COLOR_TEXT);
        int y = STATUS_Y + 9;
        for (net.minecraft.util.FormattedCharSequence line : font.split(status, (int) (PANEL_WIDTH / SMALL))) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(guiLeft + PANEL_X, guiTop + y, 300);
            guiGraphics.pose().scale(SMALL, SMALL, 1F);
            guiGraphics.drawString(font, line, 0, 0, color, false);
            guiGraphics.pose().popPose();
            y += 8;
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
                        lines.add(Component.literal(ExpressionPrinter.printNode(editor.getExpression(), child, this::getLabel)));
                    }
                    ExpressionTypeChecker.Error error = editor.check().getError(editor.getEditPath(), i);
                    if (error != null) {
                        lines.add(error.message().copy().withStyle(ChatFormatting.RED));
                    }
                }
                if (temporaryInputSlots.getItem(i).isEmpty()) {
                    lines.addAll(getValueTypeTooltip(valueTypes[i]));
                    lines.add(Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_SLOT_TOOLTIP).withStyle(ChatFormatting.GRAY));
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
                        : java.util.Objects.requireNonNullElse(ChatFormatting.getByName(line.getStyle().getColor().serialize()), ChatFormatting.WHITE);
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
