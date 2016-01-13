package org.cyclops.integrateddynamics.api.client.gui.subgui;

import net.minecraft.client.gui.Gui;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;

import java.util.List;

/**
 * An element inside the logic programmer.
 * @param <G> The type of gui.
 * @param <C> The type of container.
 * @param <S> The sub gui box type.
 * @author rubensworks
 */
public interface IGuiInputElement<S extends ISubGuiBox, G extends Gui, C extends Container> {

    /**
     * @return Localized name used for rendering.
     */
    public String getLocalizedNameFull();

    /**
     * @param lines The list to add tooltip lines to.
     */
    public void loadTooltip(List<String> lines);

    /**
     * @return The render pattern.
     */
    public IConfigRenderPattern getRenderPattern();

    /**
     * Called when this element is activated.
     */
    public void activate();

    /**
     * Called when this element is deactivated.
     */
    public void deactivate();

    /**
     * Validates the current state of the element.
     * @return An error or null.
     */
    public L10NHelpers.UnlocalizedString validate();

    /**
     * @return The color used to identify this element.
     */
    public int getColor();

    /**
     * @return The symbol used to identify this element.
     */
    public String getSymbol();

    /**
     * @param baseX Base x
     * @param baseY Base y
     * @param maxWidth Max width
     * @param maxHeight Max height
     * @param gui The parent gui
     * @param container The parent container
     * @return A subgui that is shown when activated.
     */
    @SideOnly(Side.CLIENT)
    public S createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  G gui, C container);

}
