package org.cyclops.integrateddynamics.core.part.write;

import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;

/**
 * A value holder for an {@link org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter}.
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
     * @return A pair of the source part id and part aspect to get the variable for.
     */
    public Pair<Integer, IAspect> getCurrentAspectInfo();

    /**
     * Indicate that this state should eventually recheck its aspect info because something might have changed what can
     * cause the active variable to be referring to something else.
     * @param partType The part type.
     * @param target The target.
     * @param newAspect The new active aspect, can be null.
     */
    public void triggerAspectInfoUpdate(P partType, PartTarget target, IAspectWrite newAspect);

    /**
     * @return The currently active aspect for this part, can be null.
     */
    public IAspectWrite getActiveAspect();

}
