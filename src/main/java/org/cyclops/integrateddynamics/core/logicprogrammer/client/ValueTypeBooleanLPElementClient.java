package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeBooleanRenderPattern;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeBooleanLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
public class ValueTypeBooleanLPElementClient extends ValueTypeLPElementBaseClient<ValueTypeBooleanLPElement> {

    public ValueTypeBooleanLPElementClient(ValueTypeBooleanLPElement element) {
        super(element);
    }

    @Override
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new GuiElementValueTypeBooleanRenderPattern<RenderPattern, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>(this.getElement().getInnerGuiElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
