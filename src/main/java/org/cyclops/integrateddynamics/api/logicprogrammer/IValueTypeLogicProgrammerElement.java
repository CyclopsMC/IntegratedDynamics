package org.cyclops.integrateddynamics.api.logicprogrammer;

import net.minecraft.client.gui.screens.Screen;
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
public interface IValueTypeLogicProgrammerElement<S extends ISubGuiBox, G extends Screen, C extends AbstractContainerMenu> extends ILogicProgrammerElement<S, G, C> {

    /**
     * @return The value type of this element.
     */
    public IValueType<?> getValueType();

    /**
     * @return The current value.
     */
    public IValue getValue();

    /**
     * @param value                 The new value.
     */
    public void setValue(IValue value);

    /**
     * @return Create an inner gui element for modifying the value, may be null if it doesn't apply.
     * @param <G2> The type of gui.
     * @param <C2> The type of container.
     */
    @Nullable
    public <G2 extends Screen, C2 extends AbstractContainerMenu> IGuiInputElementValueType<?, G2, C2> createInnerGuiElement();

}
