package org.cyclops.integrateddynamics.api.client.gui.subgui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * @author rubensworks
 */
public interface IGuiInputElementClient<S extends ISubGuiBox, G extends Screen, C extends AbstractContainerMenu> {

    /**
     * @param baseX Base x
     * @param baseY Base y
     * @param maxWidth Max width
     * @param maxHeight Max height
     * @param gui The parent gui
     * @param container The parent container
     * @return A subgui that is shown when activated.
     */
    public S createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                          G gui, C container);

}
