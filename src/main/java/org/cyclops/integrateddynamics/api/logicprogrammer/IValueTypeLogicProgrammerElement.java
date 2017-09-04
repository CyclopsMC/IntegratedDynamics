package org.cyclops.integrateddynamics.api.logicprogrammer;

import net.minecraft.client.gui.Gui;
import net.minecraft.inventory.Container;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

/**
 * An element instantiation of a value type inside the logic programmer.
 * @param <G> The type of gui.
 * @param <C> The type of container.
 * @param <S> The sub gui box type.
 * @author rubensworks
 */
public interface IValueTypeLogicProgrammerElement<S extends ISubGuiBox, G extends Gui, C extends Container> extends ILogicProgrammerElement<S, G, C> {

    /**
     * @return The value type of this element.
     */
    public IValueType<?> getValueType();

    /**
     * @return The current value.
     */
    public IValue getValue();

    /**
     * Set the currently stored value in the given sub gui.
     * This is useful when the gui is reused for multiple elements where the actual value is stored in this element.
     * @param subGui The sub gui to put the currently stored value in.
     */
    public void setValueInGui(S subGui);

}
