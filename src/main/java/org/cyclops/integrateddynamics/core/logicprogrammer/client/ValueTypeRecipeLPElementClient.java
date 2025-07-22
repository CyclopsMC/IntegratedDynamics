package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeRecipeLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
public class ValueTypeRecipeLPElementClient extends ValueTypeLPElementBaseClient<ValueTypeRecipeLPElement> {

    public ValueTypeRecipeLPElementMasterSubGui lastGui;

    public ValueTypeRecipeLPElementClient(ValueTypeRecipeLPElement element) {
        super(element);
    }

    public void refreshPropertiesGui(int slot) {
        if (this.lastGui != null && this.lastGui.isPropertySubGuiActive(slot)) {
            this.lastGui.propertiesSubGuis.get(slot).loadStateToGui();
        }
    }

    public void refreshInputFluidAmountBox() {
        if (this.lastGui != null && this.lastGui.subGuiRecipe.getInputFluidAmountBox() != null) {
            this.lastGui.subGuiRecipe.getInputFluidAmountBox().setValue(getElement().getInputFluidAmount());
        }
    }

    public void refreshOutputFluidAmountBox() {
        if (this.lastGui != null && this.lastGui.subGuiRecipe.getOutputFluidAmountBox() != null) {
            this.lastGui.subGuiRecipe.getOutputFluidAmountBox().setValue(getElement().getOutputFluidAmount());
        }
    }

    public void setPropertySubGui(int slotId) {
        lastGui.setPropertySubGui(slotId);
    }

    @Override
    public void setValueInGui(ISubGuiBox subGui) {
        ValueTypeRecipeLPElement element = getElement();
        ValueTypeRecipeLPElementRecipeSubGui gui = ((ValueTypeRecipeLPElementMasterSubGui) subGui).getSubGuiRecipe();
        element.setValueInContainer(gui.container);
        if (gui.getInputFluidAmountBox() != null) {
            gui.getInputFluidAmountBox().setValue(element.getInputFluidAmount());
            gui.getInputEnergyBox().setValue(element.getInputEnergy());
            gui.getOutputFluidAmountBox().setValue(element.getOutputFluidAmount());
            gui.getOutputEnergyBox().setValue(element.getOutputEnergy());
        }
    }

    @Override
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return lastGui = new ValueTypeRecipeLPElementMasterSubGui(this.getElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }
}
