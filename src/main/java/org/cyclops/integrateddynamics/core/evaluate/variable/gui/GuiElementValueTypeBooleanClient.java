package org.cyclops.integrateddynamics.core.evaluate.variable.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueTypeClient;

/**
 * @author rubensworks
 */
public class GuiElementValueTypeBooleanClient<G extends Screen, C extends AbstractContainerMenu> implements IGuiInputElementValueTypeClient<GuiElementValueTypeBooleanRenderPattern, G, C> {

    private final GuiElementValueTypeBoolean<G, C> element;

    public GuiElementValueTypeBooleanClient(GuiElementValueTypeBoolean<G, C> element) {
        this.element = element;
    }

    @Override
    public void setValueInGui(GuiElementValueTypeBooleanRenderPattern subGui, boolean sendToServer) {
        if(subGui != null) {
            subGui.getCheckbox().setChecked(this.element.getInputBoolean());
            if (sendToServer) {
                subGui.sendValueToServer();
            }
        }
    }

    @Override
    public GuiElementValueTypeBooleanRenderPattern<?, G, C> createSubGui(int baseX, int baseY,
                                                                         int maxWidth, int maxHeight, G gui, C container) {
        return new GuiElementValueTypeBooleanRenderPattern<>(this.element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
