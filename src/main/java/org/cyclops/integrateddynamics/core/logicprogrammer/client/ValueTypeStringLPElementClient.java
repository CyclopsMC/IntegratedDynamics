package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeStringLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
public class ValueTypeStringLPElementClient extends ValueTypeLPElementBaseClient<ValueTypeStringLPElement> {
    public ValueTypeStringLPElementClient(ValueTypeStringLPElement element) {
        super(element);
    }

    @Override
    public boolean isFocused(ISubGuiBox subGui) {
        return ((ValueTypeStringLPElementRenderPattern) subGui).getTextField().isFocused();
    }

    @Override
    public void setFocused(ISubGuiBox subGui, boolean focused) {
        ((ValueTypeStringLPElementRenderPattern) subGui).getTextField().setFocused(focused);
    }

    @Override
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new ValueTypeStringLPElementRenderPattern(this.getElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }
}
