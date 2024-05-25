package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import com.google.common.base.Predicates;
import lombok.Data;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;
import java.util.function.Predicate;

/**
 * GUI element for boolean value types that can be read from and written to checkboxes.
 * @author rubensworks
 */
@Data
public class GuiElementValueTypeBoolean<G extends Screen, C extends AbstractContainerMenu> implements IGuiInputElementValueType<GuiElementValueTypeBooleanRenderPattern, G, C> {

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

    public boolean getDefaultInputBoolean() {
        return this.inputBoolean;
    }

    public boolean getInputBoolean() {
        return this.inputBoolean;
    }

    @Override
    public void setValue(IValue value, GuiElementValueTypeBooleanRenderPattern propertyConfigPattern) {
        setInputBoolean(((ValueTypeBoolean.ValueBoolean) value).getRawValue(), propertyConfigPattern);
    }

    public void setInputBoolean(boolean inputBoolean, GuiElementValueTypeBooleanRenderPattern subGui) {
        this.inputBoolean = inputBoolean;
        if(subGui != null) {
            subGui.getCheckbox().setChecked(inputBoolean);
        }
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
    public Component getName() {
        return Component.translatable(getValueType().getTranslationKey());
    }

    @Override
    public void loadTooltip(List<Component> lines) {
        getValueType().loadTooltip(lines, true, null);
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
        return L10NHelpers.localize(getValueType().getTranslationKey());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiElementValueTypeBooleanRenderPattern<?, G, C> createSubGui(int baseX, int baseY,
                                                                        int maxWidth, int maxHeight, G gui, C container) {
        return new GuiElementValueTypeBooleanRenderPattern<>(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
