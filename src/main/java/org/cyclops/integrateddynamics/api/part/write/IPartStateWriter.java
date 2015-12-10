package org.cyclops.integrateddynamics.api.part.write;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.core.network.Network;

import java.util.List;

/**
 * A value holder for an {@link IPartTypeWriter}.
 * This is what will be serialized from and to NBT.
 * This object is mutable and should not be recreated.
 * @author rubensworks
 */
public interface IPartStateWriter<P extends IPartTypeWriter> extends IPartState<P> {

    /**
     * @return The inventory for this state.
     */
    public SimpleInventory getInventory();

    /**
     * @return If there is an active variable present for this state.
     */
    public boolean hasVariable();

    /**
     * Get the current target variable.
     * @return The active variable to read from.
     * @param network The network this part belongs to.
     * @param <V> The value type.
     * @return The variable.
     */
    public <V extends IValue> IVariable<V> getVariable(Network network);

    /**
     * Indicate that this state should eventually recheck its aspect info because something might have changed what can
     * cause the active variable to be referring to something else.
     * @param partType The part type.
     * @param target The target.
     * @param newAspect The new active aspect, can be null.
     */
    public void triggerAspectInfoUpdate(P partType, PartTarget target, IAspectWrite newAspect);

    /**
     * Called when this part should refresh its state.
     * When for example some variables in the network are changed.
     * @param partType The part type.
     * @param target The target.
     */
    public void onVariableContentsUpdated(P partType, PartTarget target);

    /**
     * @return The currently active aspect for this part, can be null.
     */
    public IAspectWrite getActiveAspect();

    /**
     * Get the current error for the given aspect.
     * @param aspect The aspect to get the error from.
     * @return The current error, can be empty.
     */
    public List<L10NHelpers.UnlocalizedString> getErrors(IAspectWrite aspect);

    /**
     * Set the current error for the given aspect.
     * @param aspect The aspect to set the error for.
     * @param error The error to set, or null to clear.
     */
    public void addError(IAspectWrite aspect, L10NHelpers.UnlocalizedString error);

    /**
     * @return If this part has been deactivated.
     */
    public boolean isDeactivated();

    /**
     * Used to avoid calling deactivation logic more than once when updating aspects.
     * @param deactivated If this part should be deactivated.
     */
    public void setDeactivated(boolean deactivated);

}
