package org.cyclops.integrateddynamics.core.part.aspect.build;

import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;

/**
 * Callback for {@link org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite#onDeactivate(IPartTypeWriter, PartTarget, IPartStateWriter)}.
 * @author rubensworks
 */
public interface IAspectWriteDeactivator {

    /**
     * When this aspect has become inactive.
     * @param partType The part type.
     * @param target The position that is targeted by the given part.
     * @param state The current state of the given part.
     * @param <P> The part type type.
     * @param <S> The part state.
     */
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType,
                                                                                              PartTarget target, S state);

}
