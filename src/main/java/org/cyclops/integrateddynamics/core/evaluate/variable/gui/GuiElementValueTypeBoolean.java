package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import com.google.common.base.Predicates;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * GUI element for boolean value types that can be read from and written to checkboxes.
 * @author rubensworks
 */
public class GuiElementValueTypeBoolean<G extends Screen, C extends AbstractContainerMenu> implements IGuiInputElementValueType<GuiElementValueTypeBooleanRenderPattern, G, C, GuiElementValueTypeBooleanClient<G, C>> {

    private final ValueTypeBoolean valueType;
    private Predicate<IValue> validator;
    private final IConfigRenderPattern renderPattern;
    private final boolean defaultInputBoolean;
    private boolean inputBoolean;

    public GuiElementValueTypeBoolean(ValueTypeBoolean valueType, IConfigRenderPattern renderPattern) {
        this.valueType = valueType;
        this.validator = Predicates.alwaysTrue();
        this.renderPattern = renderPattern;
        defaultInputBoolean = valueType.getDefault().getRawValue();
    }

    public ValueTypeBoolean getValueType() {
        return valueType;
    }

    public Predicate<IValue> getValidator() {
        return validator;
    }

    public boolean getDefaultInputBoolean() {
        return defaultInputBoolean;
    }

    public boolean isInputBoolean() {
        return inputBoolean;
    }

    public boolean getInputBoolean() {
        return this.inputBoolean;
    }

    @Override
    public void setValue(IValue value) {
        setInputBoolean(((ValueTypeBoolean.ValueBoolean) value).getRawValue());
    }

    public void setInputBoolean(boolean inputBoolean) {
        this.inputBoolean = inputBoolean;
    }

    @Override
    public void setValidator(Predicate<IValue> validator) {
        this.validator = validator;
    }

    @Override
    public IValue getValue() {
        return ValueTypeBoolean.ValueBoolean.of(getInputBoolean());
    }

    @Override
    public GuiElementValueTypeBooleanClient<G, C> getClient() {
        return new GuiElementValueTypeBooleanClient<>(this);
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
        this.inputBoolean = defaultInputBoolean;
    }

    @Override
    public void deactivate() {
        // Do nothing
    }

    @Override
    public Component validate() {
        if (!this.validator.test(ValueTypeBoolean.ValueBoolean.of(inputBoolean))) {
            return Component.translatable(L10NValues.VALUE_ERROR);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuiElementValueTypeBoolean<?, ?> that = (GuiElementValueTypeBoolean<?, ?>) o;
        return defaultInputBoolean == that.defaultInputBoolean
                && inputBoolean == that.inputBoolean
                && Objects.equals(valueType, that.valueType)
                && Objects.equals(validator, that.validator)
                && Objects.equals(renderPattern, that.renderPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueType, validator, renderPattern, defaultInputBoolean, inputBoolean);
    }

    @Override
    public String toString() {
        return "GuiElementValueTypeBoolean(valueType=" + valueType + ", validator=" + validator + ", renderPattern=" + renderPattern + ", defaultInputBoolean=" + defaultInputBoolean + ", inputBoolean=" + inputBoolean + ")";
    }

}
