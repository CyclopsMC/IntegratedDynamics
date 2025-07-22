package org.cyclops.integrateddynamics.api.logicprogrammer;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementClient;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;

/**
 * @author rubensworks
 */
public interface ILogicProgrammerElementClient<S extends ISubGuiBox, G extends Screen, C extends AbstractContainerMenu> extends IGuiInputElementClient<S, G, C> {

    /**
     * @param subGui The corresponding sub gui of this element.
     * @return If this element has the active focus. For typing and things like that.
     */
    public boolean isFocused(S subGui);

    /**
     * Set the focus of this element.
     * @param subGui The corresponding sub gui of this element.
     * @param focused If it must be focused.
     */
    public void setFocused(S subGui, boolean focused);

    /**
     * Set the currently stored value in the given sub gui.
     * This is useful when the gui is reused for multiple elements where the actual value is stored in this element.
     * @param subGui The sub gui to put the currently stored value in.
     */
    public void setValueInGui(S subGui);

}
