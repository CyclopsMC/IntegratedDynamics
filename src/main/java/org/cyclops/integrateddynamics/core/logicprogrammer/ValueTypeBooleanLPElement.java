package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeBooleanRenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import javax.annotation.Nullable;

/**
 * Element for the boolean value type that is controlled via a checkbox.
 * @author rubensworks
 */
public class ValueTypeBooleanLPElement extends ValueTypeLPElementBase {

    private GuiElementValueTypeBoolean<ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> innerGuiElement;

    public ValueTypeBooleanLPElement(IValueType valueType) {
        super(valueType);
        this.innerGuiElement = createInnerGuiElement();
    }

    @Nullable
    @Override
    public <G2 extends Screen, C2 extends AbstractContainerMenu> GuiElementValueTypeBoolean<G2, C2> createInnerGuiElement() {
        return new GuiElementValueTypeBoolean<>((ValueTypeBoolean) getValueType(), getRenderPattern());
    }

    @Override
    public GuiElementValueTypeBoolean<ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> getInnerGuiElement() {
        return innerGuiElement;
    }

    @Override
    public void activate() {
        getInnerGuiElement().activate();
    }

    @Override
    public void deactivate() {
        getInnerGuiElement().deactivate();
    }

    @Override
    public Component validate() {
        return getInnerGuiElement().validate();
    }

    @Override
    public IValue getValue() {
        return getInnerGuiElement().getValue();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new GuiElementValueTypeBooleanRenderPattern<RenderPattern, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>(this.getInnerGuiElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
