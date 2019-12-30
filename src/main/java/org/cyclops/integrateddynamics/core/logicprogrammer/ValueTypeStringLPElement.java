package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Element for value types that can be read from and written to strings.
 * @author rubensworks
 */
public class ValueTypeStringLPElement extends ValueTypeLPElementBase {

    public ValueTypeStringLPElement(IValueType valueType) {
        super(valueType);
    }

    @Override
    public boolean canWriteElementPre() {
        return getInnerGuiElement().getInputString() != null;
    }

    @Override
    public boolean canCurrentlyReadFromOtherItem() {
        return this.getInnerGuiElement().getInputString() == null || this.getInnerGuiElement().getInputString().equals(getInnerGuiElement().getDefaultInputString());
    }

    @Override
    public void activate() {
        getInnerGuiElement().setInputString(getInnerGuiElement().getDefaultInputString());
    }

    @Override
    public void deactivate() {
        getInnerGuiElement().setInputString(null);
    }

    @Override
    public ITextComponent validate() {
        try {
            ValueHelpers.parseString(getInnerGuiElement().getValueType(), getInnerGuiElement().getInputString());
        } catch (EvaluationException e) {
            return e.getErrorMessage();
        }
        return null;
    }

    @Override
    public IValue getValue() {
        try {
            return ValueHelpers.parseString(getInnerGuiElement().getValueType(), getInnerGuiElement().getInputString());
        } catch (EvaluationException e) {
            // Should not occur, as validation must've happened before.
            return getInnerGuiElement().getValueType().getDefault();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFocused(ISubGuiBox subGui) {
        return ((ValueTypeLPElementRenderPattern) subGui).getSearchField().isFocused();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setFocused(ISubGuiBox subGui, boolean focused) {
        ((ValueTypeLPElementRenderPattern) subGui).getSearchField().focused = focused;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new ValueTypeLPElementRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
