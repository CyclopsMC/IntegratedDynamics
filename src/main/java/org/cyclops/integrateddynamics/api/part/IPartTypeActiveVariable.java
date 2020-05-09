package org.cyclops.integrateddynamics.api.part;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;

/**
 * A part type that can have an active variable.
 * @author rubensworks
 */
public interface IPartTypeActiveVariable<P extends IPartTypeActiveVariable<P, S>, S extends IPartState<P>> extends IPartType<P, S> {

    /**
     * @param network The network this part belongs to.
     * @param target The target block.
     * @param partState The state of this part.
     * @return If there is an active variable present.
     */
    public boolean hasActiveVariable(IPartNetwork network, PartTarget target, S partState);

    /**
     * Get the variable that is currently active for this part, the value in this variable will be used to write something.
     * @param <V> The value type.
     * @param network The network this part belongs to.
     * @param partNetwork The part network this part belongs to.
     * @param target The target block.
     * @param partState The state of this part.
     * @return The variable reference to some other value that needs to be written by this part.
     */
    public <V extends IValue> IVariable<V> getActiveVariable(INetwork network, IPartNetwork partNetwork, PartTarget target, S partState);

}
