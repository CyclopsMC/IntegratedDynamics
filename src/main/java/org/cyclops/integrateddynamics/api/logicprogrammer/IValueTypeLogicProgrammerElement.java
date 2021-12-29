package org.cyclops.integrateddynamics.api.logicprogrammer;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElementValueType;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;

/**
 * An element instantiation of a value type inside the logic programmer.
 * @param <G> The type of gui.
 * @param <C> The type of container.
 * @param <S> The sub gui box type.
 * @author rubensworks
 */
public interface IValueTypeLogicProgrammerElement<S extends ISubGuiBox, G extends GuiComponent, C extends AbstractContainerMenu> extends ILogicProgrammerElement<S, G, C> {

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

    /**
     * @return Create an inner gui element for modifying the value, may be null if it doesn't apply.
     * @param <G2> The type of gui.
     * @param <C2> The type of container.
     */
    @Nullable
    public <G2 extends GuiComponent, C2 extends AbstractContainerMenu> IGuiInputElementValueType<?, G2, C2> createInnerGuiElement();

}
