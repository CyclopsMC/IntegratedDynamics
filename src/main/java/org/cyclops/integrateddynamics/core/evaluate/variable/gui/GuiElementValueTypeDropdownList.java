package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import com.google.common.base.Predicates;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueType;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntryListener;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * GUI element for value type that are displayed using a dropdown list.
 * @author rubensworks
 */
public class GuiElementValueTypeDropdownList<T, G extends Screen, C extends AbstractContainerMenu> implements IGuiInputElementValueType<GuiElementValueTypeDropdownListRenderPattern, G, C, GuiElementValueTypeDropdownListClient<T, G, C>>, IDropdownEntryListener<T> {

    private final IValueType valueType;
    private Predicate<IValue> validator;
    private final IConfigRenderPattern renderPattern;
    private String inputString;
    private Set<IDropdownEntry<T>> dropdownPossibilities = Collections.emptySet();
    private IDropdownEntryListener<T> dropdownEntryListener = null;

    public GuiElementValueTypeDropdownList(IValueType valueType, IConfigRenderPattern renderPattern) {
        this.valueType = valueType;
        this.validator = Predicates.alwaysTrue();
        this.renderPattern = renderPattern;
    }

    public IValueType getValueType() {
        return valueType;
    }

    public Predicate<IValue> getValidator() {
        return validator;
    }

    public IConfigRenderPattern getRenderPattern() {
        return renderPattern;
    }

    public String getInputString() {
        return inputString;
    }

    public Set<IDropdownEntry<T>> getDropdownPossibilities() {
        return dropdownPossibilities;
    }

    public IDropdownEntryListener<T> getDropdownEntryListener() {
        return dropdownEntryListener;
    }

    @Override
    public void setValidator(Predicate<IValue> validator) {
        this.validator = validator;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public void setDropdownPossibilities(Set<IDropdownEntry<T>> dropdownPossibilities) {
        this.dropdownPossibilities = dropdownPossibilities;
    }

    public void setDropdownEntryListener(IDropdownEntryListener<T> dropdownEntryListener) {
        this.dropdownEntryListener = dropdownEntryListener;
    }

    @Override
    public void setValue(IValue value) {
        throw new UnsupportedOperationException("This method has not been implemented yet");
    }

    @Override
    public IValue getValue() {
        throw new UnsupportedOperationException("This method has not been implemented yet");
    }

    @Override
    public GuiElementValueTypeDropdownListClient<T, G, C> getClient() {
        return new GuiElementValueTypeDropdownListClient<>(this);
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
        this.inputString = "";
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

    @Override
    public void onSetDropdownPossiblity(IDropdownEntry dropdownEntry) {
        if (dropdownEntryListener != null) {
            dropdownEntryListener.onSetDropdownPossiblity(dropdownEntry);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuiElementValueTypeDropdownList<?, ?, ?> that = (GuiElementValueTypeDropdownList<?, ?, ?>) o;
        return Objects.equals(valueType, that.valueType)
                && Objects.equals(validator, that.validator)
                && Objects.equals(renderPattern, that.renderPattern)
                && Objects.equals(inputString, that.inputString)
                && Objects.equals(dropdownPossibilities, that.dropdownPossibilities)
                && Objects.equals(dropdownEntryListener, that.dropdownEntryListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueType, validator, renderPattern, inputString, dropdownPossibilities, dropdownEntryListener);
    }

    @Override
    public String toString() {
        return "GuiElementValueTypeDropdownList(valueType=" + valueType + ", validator=" + validator + ", renderPattern=" + renderPattern + ", inputString=" + inputString + ", dropdownPossibilities=" + dropdownPossibilities + ", dropdownEntryListener=" + dropdownEntryListener + ")";
    }
}
