package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;
import java.util.function.BooleanSupplier;

import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.BUTTONS_Y;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.CHIP_HEIGHT;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.PANEL_WIDTH;
import static org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElementRenderPattern.PANEL_X;

/**
 * The buttons of the expression panel: Undo, Redo and Copy right of the title, and the button to add a parameter after the chips.
 * The panel of an expression and the panel without an operator both have them, so the panel looks the same before and after
 * an expression is typed.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ExpressionPanelButtons {

    private static final int COPIED_MS = 1500;

    final ButtonText addParameter;
    final ButtonIcon undo;
    final ButtonIcon redo;
    final ButtonIcon copy;
    private long copiedTime = 0;

    /**
     * @param firstParameter If adding a parameter adds the first one, so the button says what it adds, as there are no chips next to it.
     * @param addParameter Adds a parameter.
     * @param undo Goes back one change.
     * @param redo Goes forward one change.
     * @param copy Copies the text.
     */
    public ExpressionPanelButtons(BooleanSupplier firstParameter, Runnable addParameter, Runnable undo, Runnable redo, Runnable copy) {
        Component add = Component.literal("+");
        Component addFirst = Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_ADD_FIRST);
        this.addParameter = new ButtonText(0, 0, 12, CHIP_HEIGHT, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_ADD),
                add, button -> addParameter.run(), true) {
            @Override
            public Component getText() {
                return firstParameter.getAsBoolean() ? addFirst : add;
            }
        };
        this.addParameter.setTooltip(Tooltip.create(Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_PARAM_ADD)));
        this.undo = createButton(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TEXT_UNDO, undo, ButtonIcon.Icon.UNDO);
        this.redo = createButton(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TEXT_REDO, redo, ButtonIcon.Icon.REDO);
        this.copy = new ButtonIcon(0, 0, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TEXT_COPY),
                () -> Util.getMillis() - copiedTime < COPIED_MS ? ButtonIcon.Icon.CHECK : ButtonIcon.Icon.COPY, b -> {
            copy.run();
            copiedTime = Util.getMillis();
        });
        this.copy.setTooltip(Tooltip.create(Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TEXT_COPY + ".tooltip")));
    }

    /**
     * @param key The translation key of its narration, and of its tooltip with ".tooltip" appended.
     */
    private static ButtonIcon createButton(String key, Runnable action, ButtonIcon.Icon icon) {
        ButtonIcon button = new ButtonIcon(0, 0, Component.translatable(key), () -> icon, b -> action.run());
        button.setTooltip(Tooltip.create(Component.translatable(key + ".tooltip")));
        return button;
    }

    /**
     * @return All buttons.
     */
    public List<Button> getAll() {
        return List.of(addParameter, undo, redo, copy);
    }

    /**
     * Place the visible ones of Undo, Redo and Copy right of the title, aligned to the right,
     * and fit the button to add a parameter to its text, before the chips are placed around it.
     * @param guiLeft The left of the gui.
     * @param guiTop The top of the gui.
     */
    public void layout(int guiLeft, int guiTop) {
        int x = guiLeft + PANEL_X + PANEL_WIDTH;
        for (ButtonIcon button : List.of(copy, redo, undo)) {
            if (button.visible) {
                x -= button.getWidth();
                button.setPosition(x, guiTop + BUTTONS_Y);
                x -= 2;
            }
        }
        addParameter.setWidth(Minecraft.getInstance().font.width(addParameter.getText()) + 6);
    }

}
