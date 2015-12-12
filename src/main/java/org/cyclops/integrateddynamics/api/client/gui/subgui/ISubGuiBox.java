package org.cyclops.integrateddynamics.api.client.gui.subgui;

import org.cyclops.integrateddynamics.core.client.gui.subgui.ISubGui;

/**
 * A subgui in a box shape.
 * @author rubensworks
 */
public interface ISubGuiBox extends ISubGui {

    public int getX();
    public int getY();
    public int getWidth();
    public int getHeight();

}
