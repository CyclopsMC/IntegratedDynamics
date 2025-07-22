package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueTypeClient;

/**
 * @author rubensworks
 */
public class GuiElementValueTypeDropdownListClient<T, G extends Screen, C extends AbstractContainerMenu> implements IGuiInputElementValueTypeClient<GuiElementValueTypeDropdownListRenderPattern, G, C> {

    private final GuiElementValueTypeDropdownList<T, G, C> element;

    public GuiElementValueTypeDropdownListClient(GuiElementValueTypeDropdownList<T, G, C> element) {
        this.element = element;
    }

    @Override
    public void setValueInGui(GuiElementValueTypeDropdownListRenderPattern subGui, boolean sendToServer) {
        throw new UnsupportedOperationException("This method has not been implemented yet");
    }

    @Override
    public GuiElementValueTypeDropdownListRenderPattern<T, ?, G, C> createSubGui(int baseX, int baseY,
                                                                                 int maxWidth, int maxHeight, G gui, C container) {
        return new GuiElementValueTypeDropdownListRenderPattern<>(this.element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
