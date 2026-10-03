package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementClient;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.DecodeLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
public class DecodeLPElementClient
        implements ILogicProgrammerElementClient<ISubGuiBox, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private final DecodeLPElement element;

    public DecodeLPElementClient(DecodeLPElement element) {
        this.element = element;
    }

    public DecodeLPElement getElement() {
        return element;
    }

    @Override
    public boolean isFocused(ISubGuiBox subGui) {
        return ((DecodeLPElementRenderPattern) subGui).getTextField().isFocused();
    }

    @Override
    public void setFocused(ISubGuiBox subGui, boolean focused) {
        ((DecodeLPElementRenderPattern) subGui).getTextField().setFocused(focused);
    }

    @Override
    public void setValueInGui(ISubGuiBox subGui) {

    }

    @Override
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new DecodeLPElementRenderPattern(this.element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
