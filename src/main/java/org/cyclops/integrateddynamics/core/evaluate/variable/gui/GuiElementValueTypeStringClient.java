package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueTypeClient;

/**
 * @author rubensworks
 */
public class GuiElementValueTypeStringClient<G extends Screen, C extends AbstractContainerMenu> implements IGuiInputElementValueTypeClient<GuiElementValueTypeStringRenderPattern, G, C> {

    private final GuiElementValueTypeString<G, C> element;

    public GuiElementValueTypeStringClient(GuiElementValueTypeString<G, C> element) {
        this.element = element;
    }

    @Override
    public void setValueInGui(GuiElementValueTypeStringRenderPattern subGui, boolean sendToServer) {
        if(subGui != null) {
            subGui.getTextField().setValue(this.element.getInputString());
            if (sendToServer) {
                subGui.sendValueToServer();
            }
        }
    }

    @Override
    public GuiElementValueTypeStringRenderPattern<?, G, C> createSubGui(int baseX, int baseY,
                                                                        int maxWidth, int maxHeight, G gui, C container) {
        return new GuiElementValueTypeStringRenderPattern<>(this.element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
