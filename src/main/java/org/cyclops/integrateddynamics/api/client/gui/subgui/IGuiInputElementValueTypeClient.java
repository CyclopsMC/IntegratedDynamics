package org.cyclops.integrateddynamics.api.client.gui.subgui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * @author rubensworks
 */
public interface IGuiInputElementValueTypeClient<S extends ISubGuiBox, G extends Screen, C extends AbstractContainerMenu> extends IGuiInputElementClient<S, G, C> {

    /**
     * Set the currently stored value in the given sub gui.
     * This is useful when the gui is reused for multiple elements where the actual value is stored in this element.
     * @param subGui The sub gui to put the currently stored value in.
     * @param sendToServer If the value must be sent to the server.
     */
    public void setValueInGui(S subGui, boolean sendToServer);

}
