package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionEditor;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.Expression;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParseException;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.Region;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerExpressionActionPacket;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.CHIP_HEIGHT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_EMPTY;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_TEXT_ERROR;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.COLOR_TEXT_LIGHT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.CONTENT_BOTTOM;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.FOOTER_Y;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.PANEL_WIDTH;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.PANEL_X;
import static org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering.PARAMS_Y;

/**
 * The expression panel while no operator is active.
 * It looks like the panel of an expression without parameters.
 * @author rubensworks
 */
public class ExpressionStartPanel {

    // Below the button to add a parameter, as in the panel of an expression without parameters
    private static final int TEXT_BOX_Y = PARAMS_Y + CHIP_HEIGHT + 2;

    private final ContainerScreenLogicProgrammerBase<?> gui;
    private final ContainerLogicProgrammerBase container;
    private final Text text;
    private final List<Region> regions = new ArrayList<>();
    // Redo has nothing to redo, as there is no history without an expression
    private final ExpressionPanelButtons buttons;

    public ExpressionStartPanel(ContainerScreenLogicProgrammerBase<?> gui, ContainerLogicProgrammerBase container) {
        this.gui = gui;
        this.container = container;
        this.text = new Text(container);
        this.buttons = new ExpressionPanelButtons(() -> true, this::addParameter, text::undo, text::redo, text::formatText, text::copyText);
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

    /**
     * Add a parameter to the typed text, as there is no expression to add it to yet.
     */
    protected void addParameter() {
        text.focusText();
        text.getTextField().addParameter(text.getParserContext());
    }

    /**
     * Apply typed text once typing pauses, or when the panel is hidden. Called each tick, outside of rendering.
     */
    public void tick() {
        if (isActive()) {
            text.tick();
        }
    }

    /**
     * Apply typed text that is not applied yet, such as when the gui closes.
     */
    public void applyPendingText() {
        if (isActive()) {
            text.applyPendingText();
        }
    }

    /**
     * Stop typing, keeping valid text, as for the panel of an expression.
     * @return If the text had focus or was typed.
     */
    public boolean cancel() {
        return isShown() && text.cancel();
    }

    // --------------- Input ---------------

    protected boolean isInTextBox(double mouseX, double mouseY) {
        int x = (int) mouseX - gui.getGuiLeft();
        int y = (int) mouseY - gui.getGuiTop();
        return x >= PANEL_X && x < PANEL_X + PANEL_WIDTH && y >= TEXT_BOX_Y && y < FOOTER_Y;
    }

    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        if (!isShown()) {
            return false;
        }
        double mouseX = evt.x();
        double mouseY = evt.y();
        ExpressionTextField field = text.getTextField();
        field.setPressed(false);
        if (field.mouseClickedCompletion(mouseX - gui.getGuiLeft() - PANEL_X - 2, mouseY - gui.getGuiTop() - TEXT_BOX_Y - 2)) {
            return true;
        }
        for (AbstractWidget button : buttons.getAll()) {
            if (button.visible && button.mouseClicked(evt, isDoubleClick)) {
                return true;
            }
        }
        if (isInTextBox(mouseX, mouseY)) {
            text.focusText();
            field.mouseClicked(mouseX - gui.getGuiLeft() - PANEL_X - 2, mouseY - gui.getGuiTop() - TEXT_BOX_Y - 2);
            field.setPressed(true);
            return true;
        }
        field.setFocused(false);
        // As for the panel of an expression, valid text is applied first, as it changes what the click would act on
        if (field.isDraft() && text.parseText() != null) {
            text.applyText();
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        ExpressionTextField field = text.getTextField();
        if (isShown() && field.isPressed()) {
            field.mouseDragged(mouseX - gui.getGuiLeft() - PANEL_X - 2, mouseY - gui.getGuiTop() - TEXT_BOX_Y - 2);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        if (isActive()) {
            text.getTextField().setPressed(false);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (!isShown()) {
            return false;
        }
        ExpressionTextField field = text.getTextField();
        if (field.mouseScrolledCompletion(mouseX - gui.getGuiLeft() - PANEL_X - 2, mouseY - gui.getGuiTop() - TEXT_BOX_Y - 2, scrollY)) {
            return true;
        }
        if (isInTextBox(mouseX, mouseY)) {
            field.scroll(scrollY);
            return true;
        }
        return false;
    }

    public boolean charTyped(CharacterEvent evt) {
        return isShown() && text.charTyped(evt);
    }

    public boolean keyPressed(KeyEvent evt) {
        return isShown() && text.keyPressed(evt);
    }

    // --------------- Rendering ---------------

    public void render(GuiGraphicsExtractor guiGraphics, Font font, int mouseX, int mouseY, float partialTicks) {
        regions.clear();
        if (!isShown()) {
            return;
        }
        int guiLeft = gui.getGuiLeft();
        int guiTop = gui.getGuiTop();
        ExpressionPanelRendering.renderTitle(guiGraphics, guiLeft, guiTop, font);
        ExpressionTextField field = text.getTextField();
        Expression expression = text.parseText();
        buttons.undo.active = field.isDraft();
        buttons.redo.active = false;
        buttons.format.active = expression != null && !field.getValue().equals(text.printExact(expression));
        buttons.layout(guiLeft, guiTop);
        buttons.addParameter.setPosition(guiLeft + PANEL_X, guiTop + PARAMS_Y);
        for (AbstractWidget button : buttons.getAll()) {
            button.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }

        // The text box, as in the panel of an expression
        field.setParserContext(text::getParserContext);
        int x = guiLeft + PANEL_X;
        int y = guiTop + TEXT_BOX_Y;
        ExpressionPanelRendering.renderTextBox(guiGraphics, x, y, guiTop + FOOTER_Y, guiTop + CONTENT_BOTTOM, field.isFocused());
        String value = field.getValue();
        if (value.isEmpty() && !field.isFocused()) {
            guiGraphics.textWithWordWrap(font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PLACEHOLDER),
                    x + 2, y + 2, PANEL_WIDTH - 4, COLOR_EMPTY);
        }
        int[] colors = new int[value.length()];
        Arrays.fill(colors, COLOR_TEXT_LIGHT);
        ExpressionParseException error = value.isEmpty() ? null : text.getParseError();
        int mark = error != null ? Math.min(error.getPosition(), value.length()) : -1;
        int markEnd = error != null ? Math.min(error.getEnd(), value.length()) : -1;
        field.render(guiGraphics, x + 2, y + 2, PANEL_WIDTH - 3, guiTop + FOOTER_Y - y - 3, colors, new int[value.length()], mark, markEnd,
                COLOR_TEXT_ERROR);
        if (error != null) {
            field.renderErrorTooltip(guiGraphics, mark, markEnd, error.getComponent(), mouseX, mouseY);
        }

        // The type of what was typed, or why it can not be used
        if (value.isBlank()) {
            ExpressionPanelRendering.renderFooter(guiGraphics, guiLeft, guiTop, font, Component.empty(), null, regions);
        } else if (expression != null) {
            ExpressionPanelRendering.renderStatus(guiGraphics, guiLeft, guiTop, font, expression, text.getParsedResult(), regions);
        } else {
            ExpressionPanelRendering.renderTextError(guiGraphics, guiLeft, guiTop, font, text.getParseError(), regions);
        }
    }

    public void renderTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        ExpressionPanelRendering.renderRegionTooltips(gui, guiGraphics, regions, mouseX, mouseY);
    }

    /**
     * The typed text, which makes the element of its root operator active when it is applied.
     */
    private class Text extends ExpressionTextController {

        private Text(ContainerLogicProgrammerBase container) {
            super(container);
        }

        @Nullable
        @Override
        protected ExpressionEditor getEditor() {
            return null;
        }

        /**
         * Apply the typed text if it parses, which makes the element of its root operator active,
         * where the text stays as typed and keeps focus.
         */
        @Override
        protected void applyText() {
            Expression expression = parseText();
            if (expression == null || container.getOperatorElement(((ExpressionNode.Operator) expression.root()).operator()) == null) {
                return;
            }
            String typed = getTextField().getValue();
            getTextField().markApplied(expression);
            CompoundTag action = ExpressionEditor.actionSet(ValueDeseralizationContext.ofClient(), expression,
                    typed.equals(printExact(expression)) ? null : typed);
            container.applyExpressionAction(action);
            IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerExpressionActionPacket(action));
        }

        /**
         * Revert the typed text, as there is no history without an expression.
         */
        @Override
        protected void undo() {
            getTextField().revert();
        }

        @Override
        protected void redo() {
        }

        /**
         * Replace the typed text by its canonical form, which is applied as any typed text.
         */
        @Override
        protected void formatText() {
            Expression expression = parseText();
            if (expression != null) {
                focusText();
                getTextField().replace(printExact(expression));
            }
        }
    }

}
