package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueType;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementClient;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
public abstract class ValueTypeLPElementBaseClient<T extends ValueTypeLPElementBase>
        implements ILogicProgrammerElementClient<ISubGuiBox, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private final T element;

    public ValueTypeLPElementBaseClient(T element) {
        this.element = element;
    }

    public T getElement() {
        return element;
    }

    @Override
    public boolean isFocused(ISubGuiBox subGui) {
        if (subGui instanceof ValueTypeStringLPElementRenderPattern) {
            return ((ValueTypeStringLPElementRenderPattern) subGui).getTextField().isFocused();
        }
        return false;
    }

    @Override
    public void setFocused(ISubGuiBox subGui, boolean focused) {
        if (subGui instanceof ValueTypeStringLPElementRenderPattern) {
            ((ValueTypeStringLPElementRenderPattern) subGui).getTextField().setFocused(focused);
        }
    }

    @Override
    public abstract ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                            ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container);

    @Override
    public void setValueInGui(ISubGuiBox subGui) {
        if (getElement().getInnerGuiElement() != null) {
            ((IGuiInputElementValueType<ISubGuiBox, ?, ?, ?>) getElement().getInnerGuiElement()).getClient().setValueInGui(subGui, true);
        }
    }
}
