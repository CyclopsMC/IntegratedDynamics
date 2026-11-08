package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetArrowedListField;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.integrateddynamics.api.evaluate.expression.tree.ExpressionNode;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

import static org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionPanelRendering.COLOR_ERROR;
import static org.cyclops.integrateddynamics.core.logicprogrammer.ExpressionPanelRendering.COLOR_TEXT;

/**
 * The row below the slots, to type the value of the selected slot, or the name of the selected parameter next to its type.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ExpressionValueEditor {

    // Relative to the gui
    private static final int X = ContainerLogicProgrammerBase.BASE_X + 2;
    private static final int Y = ContainerLogicProgrammerBase.BASE_Y + ContainerLogicProgrammerBase.MAX_HEIGHT - 18;
    // As high as the type selector, which is the one of the list element
    private static final int HEIGHT = 15;
    private static final int RIGHT = ContainerLogicProgrammerBase.BASE_X + ContainerLogicProgrammerBase.MAX_WIDTH - 2;
    private static final int MIN_WIDTH = 60;
    // The arrow buttons of the type selector and the inset of its text
    private static final int TYPE_SELECTOR_PADDING = 28;
    private static final int COLOR_TEXT_FIELD = FastColor.ARGB32.color(255, 255, 255);

    private final OperatorLPElementRenderPattern pattern;
    private final OperatorLPElement element;
    private final WidgetTextFieldExtended valueField;
    @Nullable
    private WidgetArrowedListField<IValueType<?>> typeSelector = null;
    @Nullable
    private Component error = null;

    public ExpressionValueEditor(OperatorLPElementRenderPattern pattern, OperatorLPElement element) {
        this.pattern = pattern;
        this.element = element;
        Font font = Minecraft.getInstance().font;
        this.valueField = new WidgetTextFieldExtended(font, 0, 0, getValueFieldWidth(font), HEIGHT,
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
        this.valueField.setTextColor(COLOR_TEXT_FIELD);
        this.valueField.setCanLoseFocus(true);
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
        return Math.max(MIN_WIDTH, RIGHT - X - 3 - nameWidth - TYPE_SELECTOR_PADDING);
    }

    public void init(int guiLeft, int guiTop) {
        // Their backgrounds and arrow buttons are drawn a pixel above and left of their position
        this.valueField.setX(guiLeft + X + 1);
        this.valueField.setY(guiTop + Y + 1);
        // The arrow buttons are placed when it is created
        int typeX = X + valueField.getWidth() + 3;
        this.typeSelector = new WidgetArrowedListField<>(Minecraft.getInstance().font, guiLeft + typeX, guiTop + Y + 1,
                RIGHT - typeX, HEIGHT, true, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_TYPE),
                true, ExpressionEditor.getParameterTypes());
        this.typeSelector.setListener(this::onTypeSelected);
    }

    public boolean isFocused() {
        return valueField.isVisible() && valueField.isFocused();
    }

    public void setFocused(boolean focused) {
        valueField.setFocused(focused);
    }

    /**
     * Show the name of the selected parameter, or the value of the selected slot if it can be typed, and hide it otherwise.
     */
    public void update(@Nullable ExpressionEditor editor) {
        error = null;
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

    /**
     * Rename the selected parameter, or set the typed value in the selected slot.
     */
    protected void submit(ExpressionEditor editor) {
        int armed = element.getArmedParameter();
        int slot = element.getSelectedSlot();
        if (armed >= 0) {
            pattern.sendAction(ExpressionEditor.actionRenameParam(armed, valueField.getValue()));
            pattern.armParameter(-1);
        } else if (slot >= 0) {
            IValueType<?> expectedType = editor.getExpectedType(slot);
            IValue value = ExpressionEditor.parseValue(expectedType, valueField.getValue());
            if (value == null) {
                error = Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_VALUE_INVALID,
                        Component.translatable(expectedType.getTranslationKey()));
                return;
            }
            pattern.sendAction(ExpressionEditor.actionConst(ValueDeseralizationContext.ofClient(), editor.getEditPath(), slot, value));
            pattern.selectSlot(-1);
        }
    }

    /**
     * @return If the type of the selected parameter is shown right of its name.
     */
    protected boolean isTypeSelectorShown(@Nullable ExpressionEditor editor) {
        int armed = element.getArmedParameter();
        return typeSelector != null && valueField.isVisible() && editor != null && armed >= 0 && armed < editor.getExpression().parameters().size();
    }

    protected void onTypeSelected() {
        IValueType<?> type = typeSelector.getActiveElement();
        if (isTypeSelectorShown(element.getEditor()) && type != null) {
            pattern.sendAction(ExpressionEditor.actionParamType(element.getArmedParameter(), type));
        }
    }

    public boolean mouseClicked(@Nullable ExpressionEditor editor, double mouseX, double mouseY, int mouseButton) {
        if (isTypeSelectorShown(editor) && typeSelector.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        if (valueField.isVisible()) {
            if (valueField.mouseClicked(mouseX, mouseY, mouseButton)) {
                valueField.setFocused(true);
                return true;
            }
            valueField.setFocused(false);
        }
        return false;
    }

    public boolean charTyped(char typedChar, int keyCode) {
        if (!isFocused()) {
            return false;
        }
        valueField.charTyped(typedChar, keyCode);
        error = null;
        return true;
    }

    public boolean keyPressed(@Nullable ExpressionEditor editor, int typedChar, int keyCode, int modifiers) {
        if (!isFocused()) {
            return false;
        }
        if (typedChar == GLFW.GLFW_KEY_ENTER || typedChar == GLFW.GLFW_KEY_KP_ENTER) {
            if (editor != null) {
                submit(editor);
            }
            return true;
        }
        valueField.keyPressed(typedChar, keyCode, modifiers);
        error = null;
        return true;
    }

    public void render(GuiGraphics guiGraphics, int guiLeft, int guiTop, Font font, ExpressionEditor editor, int mouseX, int mouseY, float partialTicks) {
        if (!valueField.isVisible()) {
            return;
        }
        valueField.render(guiGraphics, mouseX, mouseY, partialTicks);
        int x = X + valueField.getWidth() + 3;
        // In line with the text in the field
        int y = Y + 1 + (HEIGHT - 8) / 2;
        if (isTypeSelectorShown(editor)) {
            // Follow the type, which also changes when the parameter is first dropped in a slot
            IValueType<?> type = editor.getExpression().parameters().get(element.getArmedParameter()).type();
            if (typeSelector.getActiveElement() != type) {
                typeSelector.setListener(null);
                typeSelector.setActiveElement(type);
                typeSelector.setListener(this::onTypeSelected);
            }
            typeSelector.render(guiGraphics, mouseX, mouseY, partialTicks);
        } else {
            // The type that the slot expects, or why the typed value does not fit it
            Component hint = error != null ? error : Component.translatable(editor.getExpectedType(element.getSelectedSlot()).getTranslationKey());
            float scale = Math.min(1F, (float) (RIGHT - x) / font.width(hint));
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(guiLeft + x, guiTop + y + (1 - scale) * 4, 300);
            guiGraphics.pose().scale(scale, scale, 1F);
            guiGraphics.drawString(font, hint, 0, 0, error != null ? COLOR_ERROR : COLOR_TEXT, false);
            guiGraphics.pose().popPose();
        }
    }

}
