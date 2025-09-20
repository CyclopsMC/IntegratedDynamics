package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.ingredient.ItemMatchProperties;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * @author rubensworks
 */
public class ValueTypeRecipeLPElementPropertiesSubGui extends ValueTypeRecipeAdapterLPElementPropertiesSubGui<ValueTypeRecipeLPElement> {
    public ValueTypeRecipeLPElementPropertiesSubGui(ValueTypeRecipeLPElement element, int baseX, int baseY, int maxWidth, int maxHeight, ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container, int slotId) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container, slotId);
    }

    @Override
    protected void returnToMainGui() {
        element.lastGui.setRecipeSubGui();
    }

    @Override
    public ItemStack getSlotContents() {
        return container.slots.get(slotId + ValueTypeRecipeLPElement.SLOT_OFFSET).getItem();
    }

    @Override
    public ItemMatchProperties getSlotProperties() {
        return getElement().getInputStacks().get(slotId);
    }

    @Override
    public void saveGuiToState() {
        super.saveGuiToState();
        element.sendSlotPropertiesToServer(slotId, getSlotProperties());
    }
}
