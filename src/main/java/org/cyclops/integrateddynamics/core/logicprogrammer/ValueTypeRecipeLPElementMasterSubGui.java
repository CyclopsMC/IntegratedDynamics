package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.inventory.container.ContainerExtended;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGui;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import java.util.List;

/**
 * Sub gui that holds the recipe element value type panel and the panel for setting slot properties.
 */
@OnlyIn(Dist.CLIENT)
class ValueTypeRecipeLPElementMasterSubGui extends RenderPattern<ValueTypeRecipeLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    protected final ValueTypeRecipeLPElementRecipeSubGui subGuiRecipe;
    protected final List<ValueTypeRecipeLPElementPropertiesSubGui> propertiesSubGuis;
    protected final int baseX;
    protected final int baseY;

    protected int lastGuiLeft;
    protected int lastGuiTop;
    protected ISubGui subGuiActive;

    public ValueTypeRecipeLPElementMasterSubGui(ValueTypeRecipeLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                                ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.baseX = baseX;
        this.baseY = baseY;
        this.subGuiRecipe = new ValueTypeRecipeLPElementRecipeSubGui(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.propertiesSubGuis = Lists.newArrayList();
        for (int i = 0; i < 9; i++) {
            this.propertiesSubGuis.add(new ValueTypeRecipeLPElementPropertiesSubGui(element, baseX, baseY, maxWidth, maxHeight, gui, container, i));
        }
        setSubGui(this.subGuiRecipe);
    }

    public ValueTypeRecipeLPElementRecipeSubGui getSubGuiRecipe() {
        return subGuiRecipe;
    }

    public void setSubGui(ISubGui subGui) {
        if (subGuiActive != null) {
            subGuiHolder.removeSubGui(subGuiActive);
        }
        subGuiHolder.addSubGui(subGui);
        subGuiActive = subGui;
        subGuiActive.init(lastGuiLeft, lastGuiTop);
    }

    public void setRecipeSubGui() {
        setSubGui(subGuiRecipe);

        // Restore slots
        for (int i = ValueTypeRecipeLPElement.SLOT_OFFSET; i < element.getRenderPattern().getSlotPositions().length + ValueTypeRecipeLPElement.SLOT_OFFSET; i++) {
            ContainerExtended.setSlotPosX(container.slots.get(i), 1 + getX() + element.getRenderPattern().getSlotPositions()[i - ValueTypeRecipeLPElement.SLOT_OFFSET].getLeft());
            ContainerExtended.setSlotPosY(container.slots.get(i), 1 + getY() + element.getRenderPattern().getSlotPositions()[i - ValueTypeRecipeLPElement.SLOT_OFFSET].getRight());
        }
    }

    public boolean isPropertySubGuiActive(int index) {
        return subGuiActive == this.propertiesSubGuis.get(index);
    }

    public void setPropertySubGui(int index) {
        setSubGui(this.propertiesSubGuis.get(index));

        // Hide slots (without removing them)
        for (int i = ValueTypeRecipeLPElement.SLOT_OFFSET; i < element.getRenderPattern().getSlotPositions().length + ValueTypeRecipeLPElement.SLOT_OFFSET; i++) {
            ContainerExtended.setSlotPosX(container.slots.get(i), -100);
            ContainerExtended.setSlotPosY(container.slots.get(i), -100);
        }

        // Place the selected slot in view
        ContainerExtended.setSlotPosX(container.slots.get(ValueTypeRecipeLPElement.SLOT_OFFSET + index), 1 + getX() + 116);
        ContainerExtended.setSlotPosY(container.slots.get(ValueTypeRecipeLPElement.SLOT_OFFSET + index), 1 + getY() + 2);
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);
        lastGuiLeft = guiLeft;
        lastGuiTop = guiTop;
    }

    @Override
    protected boolean drawRenderPattern() {
        return false;
    }
}
