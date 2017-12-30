package org.cyclops.integrateddynamics.api.part.write;

import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.integrateddynamics.api.part.IPartTypeActiveVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A part type for writers.
 * @author rubensworks
 */
public interface IPartTypeWriter<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> extends IPartTypeActiveVariable<P, S> {

    /**
     * @return All possible write aspects that can be used in this part type.
     */
    public List<IAspectWrite> getWriteAspects();

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
