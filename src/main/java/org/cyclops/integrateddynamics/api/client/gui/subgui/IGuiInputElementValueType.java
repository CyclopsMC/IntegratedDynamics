package org.cyclops.integrateddynamics.api.client.gui.subgui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.function.Predicate;

/**
 * A value type element inside the logic programmer.
 * @param <G> The type of gui.
 * @param <C> The type of container.
 * @param <S> The sub gui box type.
 * @author rubensworks
 */
public interface IGuiInputElementValueType<S extends ISubGuiBox, G extends Screen, C extends AbstractContainerMenu> extends IGuiInputElement<S, G, C> {

    public void setValidator(Predicate<IValue> validator);

    public IValue getValue();

    public void setValue(IValue value);

    /**
     * Set the currently stored value in the given sub gui.
     * This is useful when the gui is reused for multiple elements where the actual value is stored in this element.
     * @param subGui The sub gui to put the currently stored value in.
     * @param sendToServer If the value must be sent to the server.
     */
    @OnlyIn(Dist.CLIENT)
    public void setValueInGui(S subGui, boolean sendToServer);
}
