package org.cyclops.integrateddynamics.api.client.gui.subgui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
