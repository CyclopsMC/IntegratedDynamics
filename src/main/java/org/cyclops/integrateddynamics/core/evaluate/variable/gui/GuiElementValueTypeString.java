package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import com.google.common.base.Predicates;
import lombok.Data;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueType;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * GUI element for value type that can be read from and written to strings.
 * @author rubensworks
 */
@Data
public class GuiElementValueTypeString<G extends Screen, C extends AbstractContainerMenu> implements IGuiInputElementValueType<GuiElementValueTypeStringRenderPattern, G, C, GuiElementValueTypeStringClient<G, C>> {

    private final IValueType valueType;
    private Predicate<IValue> validator;
    private final IConfigRenderPattern renderPattern;
    private String defaultInputString;
    private String inputString;

    public GuiElementValueTypeString(IValueType valueType, IConfigRenderPattern renderPattern) {
        this.valueType = valueType;
        this.validator = Predicates.alwaysTrue();
        this.renderPattern = renderPattern;
        defaultInputString = ValueHelpers.toString(getValueType().getDefault());
    }

    @Override
    public void setValue(IValue value) {
        setInputString(ValueHelpers.toString(value));
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    @Override
    public void setValidator(Predicate<IValue> validator) {
        this.validator = validator;
    }

    @Override
    public IValue getValue() {
        try {
            return ValueHelpers.parseString(getValueType(), getInputString());
        } catch (EvaluationException e) {
            // Should not occur, as validation must've happened before.
            return getValueType().getDefault();
        }
    }

    @Override
    public GuiElementValueTypeStringClient<G, C> getClient() {
        return new GuiElementValueTypeStringClient<>(this);
    }

    @Override
    public Component getName() {
        return Component.translatable(getValueType().getTranslationKey());
    }

    @Override
    public void loadTooltip(Consumer<Component> tooltipAdder) {
        getValueType().loadTooltip(tooltipAdder, true, null);
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return renderPattern;
    }

    @Override
    public void activate() {
        this.inputString = defaultInputString;
    }

    @Override
    public void deactivate() {
        this.inputString = null;
    }

    @Override
    public Component validate() {
        try {
            IValue value = getValueType().parseString(inputString);
            if (!this.validator.test(value)) {
                return Component.translatable(L10NValues.VALUE_ERROR);
            }
        } catch (EvaluationException e) {
            return e.getErrorMessage();
        }
        return null;
    }

    @Override
    public int getColor() {
        return getValueType().getDisplayColor();
    }

    @Override
    public String getSymbol() {
        return IModHelpers.get().getL10NHelpers().localize(getValueType().getTranslationKey());
    }

}
