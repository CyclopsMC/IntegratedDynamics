package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeString;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import javax.annotation.Nullable;

/**
 * Element for value types that can be read from and written to strings.
 * @author rubensworks
 */
public class ValueTypeStringLPElement extends ValueTypeLPElementBase {

    private GuiElementValueTypeString<ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> innerGuiElement;

    public ValueTypeStringLPElement(IValueType valueType) {
        super(valueType);
        this.innerGuiElement = new GuiElementValueTypeString<>(getValueType(), getRenderPattern());
    }

    @Nullable
    @Override
    public <G2 extends Screen, C2 extends AbstractContainerMenu> GuiElementValueTypeString<G2, C2> createInnerGuiElement() {
        return new GuiElementValueTypeString<>(getValueType(), getRenderPattern());
    }

    @Override
    public GuiElementValueTypeString<ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> getInnerGuiElement() {
        return innerGuiElement;
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
    public Component validate() {
        try {
            ValueHelpers.parseString(getInnerGuiElement().getValueType(), getInnerGuiElement().getInputString());
        } catch (EvaluationException e) {
            return e.getErrorMessage();
        }
        return null;
    }

    @Override
    public IValue getValue() {
       return getInnerGuiElement().getValue();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFocused(ISubGuiBox subGui) {
        return ((ValueTypeStringLPElementRenderPattern) subGui).getTextField().isFocused();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setFocused(ISubGuiBox subGui, boolean focused) {
        ((ValueTypeStringLPElementRenderPattern) subGui).getTextField().setFocused(focused);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new ValueTypeStringLPElementRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
