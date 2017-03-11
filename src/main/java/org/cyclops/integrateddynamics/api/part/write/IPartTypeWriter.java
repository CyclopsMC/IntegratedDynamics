package org.cyclops.integrateddynamics.api.part.write;

import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A part type for writers.
 * @author rubensworks
 */
public interface IPartTypeWriter<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> extends IPartType<P, S> {

    /**
     * @return All possible write aspects that can be used in this part type.
     */
    public List<IAspectWrite> getWriteAspects();

    /**
     * Get the variable that is currently active for this part, the value in this variable will be used to write something.
     * @param <V> The value type.
     * @param network The network this part belongs to.
     * @param target The target block.
     * @param partState The state of this part.
     * @return The variable reference to some other value that needs to be written by this part.
     */
    public <V extends IValue> IVariable<V> getActiveVariable(IPartNetwork network, PartTarget target, S partState);

    /**
     * Get the aspect that is currently active in this part, can be null.
     * @param target The target block.
     * @param partState The state of this part.
     * @return The active aspect.
     */
    public IAspectWrite getActiveAspect(PartTarget target, S partState);

    /**
     * Update the active aspect and active variable for this part.
     * @param target The target block.
     * @param partState The state of this part.
     * @param player The player activating the aspect, can be null.
     */
    public void updateActivation(PartTarget target, S partState, @Nullable EntityPlayer player);

}
