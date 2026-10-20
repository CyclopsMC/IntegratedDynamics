package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParseException;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParser;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTypeChecker;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.Region;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerExpressionActionPacket;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.APPLY_DELAY_MS;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.CHIP_HEIGHT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.COLOR_EMPTY;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.COLOR_FOCUS;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.COLOR_STRIP;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.COLOR_TEXT_ERROR;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.COLOR_TEXT_LIGHT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.CONTENT_BOTTOM;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.FOOTER_Y;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.PANEL_WIDTH;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.PANEL_X;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.PARAMS_Y;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.drawOutline;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.getCallSignature;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.renderFooter;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.renderStatus;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.renderTextError;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.renderTitle;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.wrapTooltip;

/**
 * The expression panel while no operator is active, where an expression can be typed right away.
 * Applying it makes the element of its root operator active, where the text can be edited further.
 * It looks like the panel of an expression without parameters, with the same buttons, which work on the text.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ExpressionStartPanel {

    // Below the button to add a parameter, as in the panel of an expression without parameters
    private static final int TEXT_BOX_Y = PARAMS_Y + CHIP_HEIGHT + 2;

    private final ContainerScreenLogicProgrammerBase<?> gui;
    private final ContainerLogicProgrammerBase container;
    private final List<Region> regions = new ArrayList<>();
    @Nullable
    private Int2ObjectMap<IValueType<?>> inventoryCardTypes = null;
    @Nullable
    private String parsedFor = null;
    @Nullable
    private Expression parsedText = null;
    @Nullable
    private ExpressionTypeChecker.Result parsedTextResult = null;
    @Nullable
    private ExpressionParseException textError = null;

    // Redo has nothing to redo, as there is no history without an expression
    private final ExpressionPanelButtons buttons = new ExpressionPanelButtons(() -> true, this::addParameter, this::undo, () -> {},
            this::copyText);

    public ExpressionStartPanel(ContainerScreenLogicProgrammerBase<?> gui, ContainerLogicProgrammerBase container) {
        this.gui = gui;
        this.container = container;
    }

    /**
     * @return If there is no expression to show, so this panel is used instead.
     */
    public boolean isActive() {
        return !(container.getActiveElement() instanceof OperatorLPElement);
    }

    protected boolean isShown() {
        return isActive() && ContainerScreenLogicProgrammerBase.isExpressionPanelOpen();
    }

    protected ExpressionTextField getTextField() {
        return OperatorLPElementRenderPattern.getTextField(container);
    }

    // --------------- Text ---------------

    @Nullable
    protected IValueType<?> getRefType(int variableId) {
        if (inventoryCardTypes == null) {
            inventoryCardTypes = OperatorLPElement.getInventoryCardTypes(Minecraft.getInstance().player);
        }
        return inventoryCardTypes.get(variableId);
    }

    protected IntList getLabelIds(String label) {
        return LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getVariableIds(label);
    }

    protected ExpressionParser.Context getParserContext() {
        return ExpressionEditor.getParserContext(ValueDeseralizationContext.ofClient(), this::getRefType, this::getLabelIds);
    }

    /**
     * @return The typed expression, or null if it does not parse, as cached for the current text.
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

    /**
     * Apply the typed text if it parses, which makes the element of its root operator active.
     * This replaces the sub guis, so it is not done while rendering or while iterating over them.
     */
    protected void applyText() {
        Expression expression = parseText();
        if (expression == null || container.getOperatorElement(((ExpressionNode.Operator) expression.root()).operator()) == null) {
            return;
        }
        // The text stays as typed, and keeps focus, in the panel of the expression
        getTextField().markApplied(expression);
        CompoundTag action = ExpressionEditor.actionSet(ValueDeseralizationContext.ofClient(), expression);
        container.applyExpressionAction(action);
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerExpressionActionPacket(action));
    }

    /**
     * Apply typed text once typing pauses, or when the panel is hidden. Called each tick, outside of rendering.
     */
    public void tick() {
        if (!isActive()) {
            return;
        }
        ExpressionTextField field = getTextField();
        boolean panel = ContainerScreenLogicProgrammerBase.isExpressionPanelOpen();
        if (field.isDraft() && (!panel || field.getMillisSinceEdit() >= APPLY_DELAY_MS)) {
            applyText();
        }
        if (!panel && isActive()) {
            field.revert();
            field.setFocused(false);
        }
    }

    /**
     * Apply typed text that is not applied yet, such as when the gui closes.
     */
    public void applyPendingText() {
        if (isActive() && getTextField().isDraft()) {
            applyText();
        }
    }

    /**
     * Stop typing, keeping valid text, as for the panel of an expression.
     * @return If the text had focus or was typed.
     */
    public boolean cancel() {
        ExpressionTextField field = getTextField();
        if (!isShown() || (!field.isDraft() && !field.isFocused())) {
            return false;
        }
        if (field.closeCompletions()) {
            return true;
        }
        if (field.isDraft() && parseText() != null) {
            applyText();
        } else {
            field.revert();
        }
        field.setFocused(false);
        return true;
    }

    protected void focusText() {
        ExpressionTextField field = getTextField();
        if (!field.isFocused()) {
            field.setFocused(true);
            // Cards may have been added to the inventory since
            inventoryCardTypes = null;
        }
    }

    /**
     * Add a parameter to the typed text, as there is no expression to add it to yet.
     */
    protected void addParameter() {
        focusText();
        getTextField().addParameter(getParserContext());
    }

    protected void undo() {
        getTextField().revert();
    }

    protected void copyText() {
        Minecraft.getInstance().keyboardHandler.setClipboard(getTextField().getValue());
    }

    // --------------- Input ---------------

    protected boolean isInTextBox(double mouseX, double mouseY) {
        int x = (int) mouseX - gui.getGuiLeft();
        int y = (int) mouseY - gui.getGuiTop();
        return x >= PANEL_X && x < PANEL_X + PANEL_WIDTH && y >= TEXT_BOX_Y && y < FOOTER_Y;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!isShown()) {
            return false;
        }
        ExpressionTextField field = getTextField();
        field.setPressed(false);
        if (field.mouseClickedCompletion(mouseX - gui.getGuiLeft() - PANEL_X - 2, mouseY - gui.getGuiTop() - TEXT_BOX_Y - 2)) {
            return true;
        }
        for (AbstractWidget button : buttons.getAll()) {
            if (button.visible && button.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        if (isInTextBox(mouseX, mouseY)) {
            focusText();
            field.mouseClicked(mouseX - gui.getGuiLeft() - PANEL_X - 2, mouseY - gui.getGuiTop() - TEXT_BOX_Y - 2);
            field.setPressed(true);
            return true;
        }
        field.setFocused(false);
        // As for the panel of an expression, valid text is applied first, as it changes what the click would act on
        if (field.isDraft() && parseText() != null) {
            applyText();
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        ExpressionTextField field = getTextField();
        if (isShown() && field.isPressed()) {
            field.mouseDragged(mouseX - gui.getGuiLeft() - PANEL_X - 2, mouseY - gui.getGuiTop() - TEXT_BOX_Y - 2);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        if (isActive()) {
            getTextField().setPressed(false);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (isShown() && getTextField().mouseScrolledCompletion(mouseX - gui.getGuiLeft() - PANEL_X - 2,
                mouseY - gui.getGuiTop() - TEXT_BOX_Y - 2, scrollY)) {
            return true;
        }
        if (isShown() && isInTextBox(mouseX, mouseY)) {
            getTextField().scroll(scrollY);
            return true;
        }
        return false;
    }

    public boolean charTyped(char typedChar) {
        ExpressionTextField field = getTextField();
        if (isShown() && field.isFocused()) {
            field.charTyped(typedChar);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode) {
        ExpressionTextField field = getTextField();
        if (!isShown() || !field.isFocused()) {
            return false;
        }
        if (field.keyPressedCompletion(keyCode)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (Screen.hasShiftDown()) {
                field.insert("\n");
            } else if (field.isDraft()) {
                applyText();
            }
        } else if (keyCode == GLFW.GLFW_KEY_Z && Screen.hasControlDown() && !Screen.hasShiftDown()) {
            // There is no history yet, so this only reverts what was typed
            field.revert();
        } else {
            field.keyPressed(keyCode);
        }
        // Keep keys such as the inventory key from closing the gui while typing
        return true;
    }

    // --------------- Rendering ---------------

    public void render(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, float partialTicks) {
        regions.clear();
        if (!isShown()) {
            return;
        }
        int guiLeft = gui.getGuiLeft();
        int guiTop = gui.getGuiTop();
        renderTitle(guiGraphics, guiLeft, guiTop, font);
        buttons.undo.active = getTextField().isDraft();
        buttons.redo.active = false;
        buttons.layout(guiLeft, guiTop);
        buttons.addParameter.setPosition(guiLeft + PANEL_X, guiTop + PARAMS_Y);
        for (AbstractWidget button : buttons.getAll()) {
            button.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        // The text box, as in the panel of an expression
        ExpressionTextField field = getTextField();
        field.setParserContext(this::getParserContext);
        int x = guiLeft + PANEL_X;
        int y = guiTop + TEXT_BOX_Y;
        guiGraphics.fill(x, y, x + PANEL_WIDTH, guiTop + CONTENT_BOTTOM, COLOR_STRIP);
        if (field.isFocused()) {
            drawOutline(guiGraphics, x, y, PANEL_WIDTH, guiTop + FOOTER_Y - y, COLOR_FOCUS);
        }
        String text = field.getValue();
        if (text.isEmpty() && !field.isFocused()) {
            guiGraphics.drawWordWrap(font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PLACEHOLDER),
                    x + 2, y + 2, PANEL_WIDTH - 4, COLOR_EMPTY);
        }
        Expression expression = parseText();
        int[] colors = new int[text.length()];
        Arrays.fill(colors, COLOR_TEXT_LIGHT);
        int mark = expression == null && textError != null && !text.isEmpty() ? Math.min(textError.getPosition(), text.length()) : -1;
        field.render(guiGraphics, x + 2, y + 2, PANEL_WIDTH - 3, guiTop + FOOTER_Y - y - 3, colors, new int[text.length()], mark, COLOR_TEXT_ERROR);

        // The call at the cursor, the type of what was typed, or why it can not be used
        ExpressionParser.Call call = field.getCallAtCursor();
        if (call != null) {
            renderFooter(guiGraphics, guiLeft, guiTop, font, getCallSignature(call), null, regions);
        } else if (text.isBlank()) {
            renderFooter(guiGraphics, guiLeft, guiTop, font, Component.empty(), null, regions);
        } else if (expression != null) {
            renderStatus(guiGraphics, guiLeft, guiTop, font, expression, parsedTextResult, regions);
        } else {
            renderTextError(guiGraphics, guiLeft, guiTop, font, text, textError, regions);
        }
    }

    public void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (Region region : regions) {
            if (region.tooltip() != null && gui.isHovering(region.x(), region.y(), region.width(), region.height(), mouseX, mouseY)) {
                gui.drawTooltip(wrapTooltip(Lists.newArrayList(region.tooltip())), guiGraphics.pose(),
                        mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
            }
        }
    }

}
