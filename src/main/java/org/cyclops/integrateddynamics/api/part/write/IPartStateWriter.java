package org.cyclops.integrateddynamics.api.part.write;

import net.minecraft.network.chat.MutableComponent;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;

import javax.annotation.Nullable;
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
     * @param <V> The value type.
     * @param network The network this part belongs to.
     * @param partNetwork The part network this part belongs to.
     * @param valueDeseralizationContext The value deserialization context.
     * @return The active variable to read from.
     */
    public <V extends IValue> IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork, ValueDeseralizationContext valueDeseralizationContext);

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
    @Nullable
    public IAspectWrite getActiveAspect();

    /**
     * Get the current error for the given aspect.
     * @param aspect The aspect to get the error from.
     * @return The current error, can be empty.
     */
    public List<MutableComponent> getErrors(IAspectWrite aspect);

    /**
     * Set the current error for the given aspect.
     * @param aspect The aspect to set the error for.
     * @param error The error to set, or null to clear.
     */
    public void addError(IAspectWrite aspect, MutableComponent error);

    /**
     * @return If this part has been deactivated.
     */
    public boolean isDeactivated();

    /**
     * Used to avoid calling deactivation logic more than once when updating aspects.
     * @param deactivated If this part should be deactivated.
     */
    public void setDeactivated(boolean deactivated);

    /**
     * If this state is at its first tick, will be reset after calling this.
     * @return If this is the first time this state is ticking.
     */
    public boolean checkAndResetFirstTick();

}
