package org.cyclops.integrateddynamics.core.part.aspect.build;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Listens to calls to
 * {@link org.cyclops.integrateddynamics.api.part.aspect.IAspect#update(org.cyclops.integrateddynamics.api.network.INetwork, IPartNetwork, IPartType, PartTarget, IPartState)}.
 * @author rubensworks
 */
public interface IAspectUpdateListener {

    public <P extends IPartType<P, S>, S extends IPartState<P>> void onUpdate(INetwork network, IPartNetwork partNetwork, P partType, PartTarget target, S state);

    /**
     * Before the update is called.
     */
    public static interface Before extends IAspectUpdateListener {}

    /**
     * After the update was called.
     */
    public static interface After extends IAspectUpdateListener {}

}
