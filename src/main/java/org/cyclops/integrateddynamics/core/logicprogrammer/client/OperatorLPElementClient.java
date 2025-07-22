package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementClient;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
public class OperatorLPElementClient<T extends OperatorLPElement>
        implements ILogicProgrammerElementClient<RenderPattern, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private final T element;

    public OperatorLPElementClient(T element) {
        this.element = element;
    }

    public T getElement() {
        return element;
    }

    @Override
    public void setValueInGui(RenderPattern subGui) {
        this.element.setValueInContainer((ContainerLogicProgrammerBase) subGui.getContainer());
    }

    @Override
    public boolean isFocused(RenderPattern subGui) {
        return this.element.isFocused();
    }

    @Override
    public void setFocused(RenderPattern subGui, boolean focused) {
        this.element.setFocused(focused);
    }

    @Override
    public RenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                      ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new OperatorLPElementRenderPattern(this.element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
