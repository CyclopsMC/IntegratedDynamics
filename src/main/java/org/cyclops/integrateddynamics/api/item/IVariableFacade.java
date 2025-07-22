package org.cyclops.integrateddynamics.api.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A facade for retrieving a variable.
 * @author rubensworks
 */
public interface IVariableFacade {

    public IVariableFacadeClient getClient();

    /**
     * @return The unique id for this facade.
     */
    public int getId();

    /**
     * @return The optional label for this facade.
     */
    @Nullable
    public String getLabel();

    /**
     * Get the variable.
     * @param <V> The value type.
     * @param network The network used to look for the variable.
     * @param partNetwork The part network used to look for the variable.
     * @return The variable.
     */
    public <V extends IValue> IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork);

    /**
     * @return If this is a valid reference.
     */
    public boolean isValid();

    /**
     * Check if this facade is valid, otherwise notify the validator of any errors.
     * @param network The network used to look for the variable.
     * @param partNetwork The part network used to look for the variable.
     * @param validator The object to notify errors to.
     * @param containingValueType The value type in which this variable facade is being used.
     */
    public void validate(INetwork network, IPartNetwork partNetwork, IValidator validator, IValueType containingValueType);

    /**
     * @return The output type of this variable facade.
     */
    public IValueType getOutputType();

    /**
     * Add information about this variable facade to the list.
     *
     * @param tooltipAdder The list to add lines to.
     * @param context      The context.
     */
    public void appendHoverText(Consumer<Component> tooltipAdder, Item.TooltipContext context);

    public static interface IValidator {

        /**
         * Set the current error for the given aspect.
         *
         * @param error The error to set, or null to clear.
         */
        public void addError(MutableComponent error);

    }

}
