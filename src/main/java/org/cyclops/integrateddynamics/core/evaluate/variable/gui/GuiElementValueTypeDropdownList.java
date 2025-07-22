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
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntryListener;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * GUI element for value type that are displayed using a dropdown list.
 * @author rubensworks
 */
@Data
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

    @Override
    public void setValidator(Predicate<IValue> validator) {
        this.validator = validator;
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
}
